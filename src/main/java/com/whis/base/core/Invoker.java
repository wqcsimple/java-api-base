package com.whis.base.core;

/**
 * Created by dd on 7/24/16.
 */
public class Invoker {
    private String name;
    private Object[] params;

    public Invoker(String name, Object[] params)
    {
        this.name = name;
        this.params = params;
    }

    public String getName() {
        return name;
    }

    public Object[] getParams() {
        return params;
    }


}
