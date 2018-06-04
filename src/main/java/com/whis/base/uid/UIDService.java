package com.whis.base.uid;

import com.whis.base.exception.BaseException;
import com.whis.base.redis.Redis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UIDService {
    private final Redis redis;

    @Autowired
    public UIDService(Redis redis) {
        this.redis = redis;
    }

    public String get(UIDRunner runner) {
        return get(runner, 9, 3, 30);
    }

    public String get(UIDRunner runner, int maxTryGenerateCount, int maxTryLockCount, int lockTimeout) {
        String uid;
        int tryLockCount = 0;
        while (true) {
            int tryGenerateCount = 0;
            // generate
            while (true) {
                uid = runner.generate();
                if (!runner.exists(uid)) {
                    break;
                }

                tryGenerateCount++;
                if (tryGenerateCount >= maxTryGenerateCount) {
                    throw new BaseException(-1, "fail too many times when generate uid");
                }
            }

            // lock uid, cause uid from different client may be same
            String keyLock = runner.lockKey(uid);
            if (redis.lock(keyLock, lockTimeout))
            {
                return uid;
            }

            tryLockCount++;
            if (tryLockCount >= maxTryLockCount) {
                throw new BaseException(-1, "fail too many times to lock uid");
            }
        }
    }
}
