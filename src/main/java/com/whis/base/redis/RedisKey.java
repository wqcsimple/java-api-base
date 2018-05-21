package com.whis.base.redis;

import com.whis.base.model.BaseModel;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RedisKey {
    protected static String redisKeyPrefix = "";

    private static final SimpleDateFormat dayFormatter = new SimpleDateFormat("Ymd");

    public static void setKeyPrefix(String prefix) {
        redisKeyPrefix = prefix;
    }

    public static String token(String token) {
        return redisKeyPrefix + ".token." + token;
    }

    public static String user(Long id) {
        return redisKeyPrefix + ".user." + id;
    }

    public static String roleAuthorityIdList(Long id) { return redisKeyPrefix + ".role_authority_id_list." + id; }

    public static String userAuthorityIdList(Long id) { return redisKeyPrefix + ".user_authority_id_list." + id; }

    public static String userRoleIdList(Long id) { return redisKeyPrefix + ".user_role_id_list." + id; }

    public static String authority(Long id) { return redisKeyPrefix + ".authority." + id; }

    public static String key(BaseModel model) {
        return model(model.getClass(), model.ID());
    }

    public static String model(Class cls, long id) {
        return redisKeyPrefix + "." + cls.getName() + ".hash." + id;
    }


    public static String todayPayCount() {
        String date = dayFormatter.format(new Date());
        return redisKeyPrefix + ".pay.day." + date + ".count";
    }

    public static String todayTokenCount() {
        String date = dayFormatter.format(new Date());
        return redisKeyPrefix + ".token.day." + date + ".count";
    }
}
