package com.mondego.framework.pipeline;

import java.lang.reflect.InvocationTargetException;

public interface IChannle {
    public void shutdown();
    public <U> void send(U u) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
    InvocationTargetException, NoSuchMethodException, SecurityException;

}
