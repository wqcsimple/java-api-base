package com.whis.base.core;

import java.util.Map;

/**
 * Created by dd on 7/24/16.
 */
public interface ProcessModelWithArgsInterface {
    Map<String, Object> processModel(Object model, String[] keys, Object[] args);
}
