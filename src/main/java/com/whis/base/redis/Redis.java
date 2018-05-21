package com.whis.base.redis;

import com.whis.base.common.Const;
import com.whis.base.common.Util;
import com.whis.base.exception.BaseException;
import com.whis.base.model.BaseModel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import redis.clients.jedis.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dd on 7/24/16.
 */
@Component("Redis")
@Configuration
public class Redis {

    private static       Logger       logger       = LoggerFactory.getLogger(Redis.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();


    @Value("${spring.redis.host:redis}")
    private String host;

    @Value("${spring.redis.password:null}")
    private String password;

    @Value("${spring.redis.port:6379}")
    private Integer port;

    @Value("${spring.redis.database:1}")
    private Integer database;

    @Value("${spring.redis.jedis.pool.max-active:100}")
    private Integer poolMaxActive;

    @Value("${spring.redis.jedis.pool.max-idle:8}")
    private Integer poolMaxIdle;

    @Value("${spring.redis.jedis.pool.max-wait:-1}")
    private Long poolMaxWait;

    @Value("${spring.redis.jedis.pool.min-idle:0}")
    private Integer poolMinIdle;

    @Value("${spring.redis.key-prefix:}")
    private String keyPrefix;

    private JedisPool jedisPool;

    public Redis()
    {

    }

    @PostConstruct
    public void init()
    {
        password = Strings.isNullOrEmpty(password) ? null : password;

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMinIdle(poolMinIdle);
        jedisPoolConfig.setMaxIdle(poolMaxIdle);
        jedisPoolConfig.setMaxTotal(poolMaxActive);
        jedisPoolConfig.setMaxWaitMillis(poolMaxWait);
        jedisPool = new JedisPool(jedisPoolConfig, host, port, Protocol.DEFAULT_TIMEOUT, password);

        logger.info("set prefix: {}", keyPrefix);
        RedisKey.setKeyPrefix(keyPrefix);
    }

    @PreDestroy
    public void onDestroy()
    {
        if (jedisPool != null)
        {
            jedisPool.destroy();
        }
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public Jedis getClient()
    {
        return jedisPool.getResource();
    }

    private String getKeyOfLock(String key)
    {
        return String.format("%s.lock.%s", Const.REDIS_KEY_PREFIX, key);
    }

    public boolean lock(String key)
    {
        return lock(key, 60);
    }

    public boolean lock(String key, int expireSeconds)
    {
        try (Jedis jedis = getClient()) {
            String redisKey = getKeyOfLock(key);
            Long count = jedis.incrBy(redisKey, 1);
            jedis.expire(redisKey, expireSeconds);
            return count == 1;
        }
    }

    public void unlock(String key)
    {
        try (Jedis jedis = getClient()) {
            String redisKey = getKeyOfLock(key);
            jedis.del(redisKey);
        }
    }

    public <T> T get(String key, RedisGetMethodInterface redisGetMethodInterface, Class<T> type)
    {
        try (Jedis jedis = getClient()) {
            String data = jedis.get(key);
            if (Strings.isNullOrEmpty(data))
            {
                Object dataObject = redisGetMethodInterface.method();
                data = Util.jsonEncode(dataObject);
                jedis.set(key, data);
            }
            return Util.jsonDecode(data, type);
        }
    }

    public <T> T get(String key, RedisGetMethodInterface redisGetMethodInterface, Class<T> type, int expireSeconds)
    {
        try (Jedis jedis = getClient()) {
            String data = jedis.get(key);
            if (Strings.isNullOrEmpty(data))
            {
                Object dataObject = redisGetMethodInterface.method();
                data = Util.jsonEncode(dataObject);
                jedis.setex(key, expireSeconds, data);
            }
            return Util.jsonDecode(data, type);
        }
    }

    public <T> T get(String key, RedisGetMethodInterface redisGetMethodInterface, TypeReference<T> typeReference)
    {
        try (Jedis jedis = getClient()) {
            String data = jedis.get(key);
            if (Strings.isNullOrEmpty(data))
            {
                Object dataObject = redisGetMethodInterface.method();
                data = Util.jsonEncode(dataObject);
                jedis.set(key, data);
            }
            return Util.jsonDecode(data, typeReference);
        }
    }

    public <T> T get(String key, RedisGetMethodInterface redisGetMethodInterface, TypeReference<T> typeReference, int expireSeconds)
    {
        try (Jedis jedis = getClient()) {
            String data = jedis.get(key);
            if (Strings.isNullOrEmpty(data))
            {
                Object dataObject = redisGetMethodInterface.method();
                data = Util.jsonEncode(dataObject);
                jedis.setex(key, expireSeconds, data);
            }
            return Util.jsonDecode(data, typeReference);
        }
    }

    public void cache(BaseModel model) {
        String key = model.cacheKey();
        Integer expireTime = model.cacheExpireTime();
        cache(key, model, expireTime);
    }

    public void cache(BaseModel model, String key) {
        Integer expireTime = model.cacheExpireTime();
        cache(key, model, expireTime);
    }

    public void cache(String key, BaseModel model, int expireTime) {
        String[] keys = model.keys();
        Map<String, String> hash = new HashMap<>();
        for (String k : keys) {
            Object value = model.get(k);
            if (value != null) {
                hash.put(k, value.toString());
            }
        }

        try (Jedis jedis = getClient()) {
            if (expireTime > 0) {
                Transaction tx =  jedis.multi();
                tx.hmset(key, hash);
                tx.expire(key, expireTime);
                tx.exec();
            } else {
                jedis.hmset(key, hash);
            }
        }
    }

    private Map getFilteredHash(String[] keys, Map hash) {
        Map filteredHash = new HashMap();
        for (String key : keys) {
            if (!hash.containsKey(key)) {
                return null;
            }
            filteredHash.put(key, hash.get(key));
        }

        return filteredHash;
    }

    public <T> T get(String key, Class<T> type) {
        T instance;
        try {
            instance = type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new BaseException(-1, "can not new instance: " + type);
        }

        if (!(instance instanceof BaseModel)) {
            throw new BaseException(-1, "not BaseModel");
        }

        try (Jedis jedis = getClient()) {
            Map modelData = jedis.hgetAll(key);
            BaseModel model = (BaseModel) instance;
            String[] keys = model.keys();
            Map filteredData = getFilteredHash(keys, modelData);
            if (filteredData == null) {
                return null;
            } else {
                return objectMapper.convertValue(filteredData, type);
            }
        }
    }

    public <T> T get(String key, Class<T> type, RedisGetModelInterface redisGetModelInterface) {
        T instance;
        try {
            instance = type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new BaseException(-1, "can not new instance: " + type);
        }

        if (!(instance instanceof BaseModel)) {
            throw new BaseException(-1, "not BaseModel");
        }

        try (Jedis jedis = getClient()) {
            Map modelData = jedis.hgetAll(key);
            BaseModel model = (BaseModel) instance;
            String[] keys = model.keys();
            Map filteredData = getFilteredHash(keys, modelData);
            if (filteredData == null) {
                BaseModel m = redisGetModelInterface.model();
                if (m == null) {
                    return null;
                }
                m.cache();

                String baseCacheKey = RedisKey.model(type, m.ID());
                if (!m.cacheKey().equals(baseCacheKey)) {
                    cache(m, baseCacheKey);
                }

                return (T) m;
            } else {
                return objectMapper.convertValue(filteredData, type);
            }
        }
    }

    public <T> T get(Class<T> type) {
        T instance;
        try {
            instance = type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new BaseException(-1, "can not new instance: " + type);
        }

        if (!(instance instanceof BaseModel)) {
            throw new BaseException(-1, "not BaseModel");
        }

        BaseModel model = (BaseModel) instance;

        try (Jedis jedis = getClient()) {
            String key = model.cacheKey();
            Map modelData = jedis.hgetAll(key);
            return objectMapper.convertValue(modelData, type);
        }
    }

}
