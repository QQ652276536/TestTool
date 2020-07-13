package com.zistone.testtool.faceidcompare.model;

/**
 * 配置BaseConfig单例
 */
public class SingleBaseConfig {
    private static BaseConfig baseConfig;

    private SingleBaseConfig() {
    }

    public static BaseConfig getBaseConfig() {
        if (baseConfig == null) {
            baseConfig = new BaseConfig();
        }
        return baseConfig;
    }

    public static void copyInstance(BaseConfig result) {
        baseConfig = result;
    }
}
