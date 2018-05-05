package com.whis.base.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by dd on 21/07/2017.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Model {
    String tableName();
    boolean enableWeight() default true;
    boolean enableCache() default true;
}
