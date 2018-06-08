package com.whis.base.misc.uid;

public interface UIDRunner {
    String generate();
    Boolean exists(String uid);
    String lockKey(String uid);
}
