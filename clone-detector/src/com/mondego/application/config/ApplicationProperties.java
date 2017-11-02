package com.mondego.application.config;

import java.util.Arrays;
import java.util.List;

import com.mondego.framework.config.FrameworkProperties;

public class ApplicationProperties {

    public static final String INDEX_DIR = FrameworkProperties.ROOT_DIR
            + "index";
    public static final String GTPM_DIR = FrameworkProperties.ROOT_DIR + "gtpm";
    public static final String GTPM_INDEX_DIR = FrameworkProperties.ROOT_DIR
            + "gtpmindex";
    public static final String QUERY_FILE_NAME = "blocks.file";
    public static final String OUTPUT_BACKUP_DIR = FrameworkProperties.ROOT_DIR
            + "backup_output";
    public static List<String> METRICS_ORDER_IN_INPUT_FILE = Arrays
            .asList("num_tokens", "num_unique_tokens");
    //public static final int MIN_TOKENS = properties.getInt("LEVEL_1_MIN_TOKENS", 65);
    //public static final int MAX_TOKENS = properties.getInt("LEVEL_1_MAX_TOKENS", 500000);
}
