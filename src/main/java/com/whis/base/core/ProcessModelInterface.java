package com.whis.base.core;

import java.util.Map;

/**
 * Created by dd on 7/24/16.
 */
public interface ProcessModelInterface {
    Map<String, Object> processModel(Object model, String[] keys);
}
