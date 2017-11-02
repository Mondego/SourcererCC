package com.mondego.framework.config;

import net.jmatrix.eproperties.EProperties;

public class FrameworkProperties {
    private EProperties properties;
    public static final String ROOT_DIR = System.getProperty("properties.rootDir");
    public static final String RUN_METADATA = FrameworkProperties.ROOT_DIR
            + "run_metadata.scc";
    public static final String SEARCH_METADATA = FrameworkProperties.ROOT_DIR
            + "search_metadata.txt";
    
    public void setProperties(EProperties properties){
        this.properties = properties;
    }
}
