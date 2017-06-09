/**
 * Copyright 2017 Comcast Cable Communications Management, LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.comcast.redirector.core.config;

import com.comcast.xre.common.redirector.selector.xmlimpl.Config;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class ConfigLoaderTest {

    private String customPropertiesName = "shell.xre.properties";
    private String failPropertiesName = "ConfigLoaderTest.java";
    private Integer integer = 10000022;
    private Long longNumber = 100000020200L;

    @Before
    public void setUp () {
        System.setProperty(ConfigLoader.DEFAULT_CONFIG_ENV_PATH, customPropertiesName);
    }

    @Test
    public void loadConfigFileTest () {
        ZKConfig zkConfig = ConfigLoader.doParse(Config.class);

        Assert.assertEquals("localhost:21824", zkConfig.getZooKeeperConnection());
        Assert.assertFalse(zkConfig.isCacheHosts());
        Assert.assertEquals("/testcases", zkConfig.getZooKeeperBasePath());
    }

    @Test
    public void loadConfigFileDefaultTest () {
        System.clearProperty(ConfigLoader.DEFAULT_CONFIG_ENV_PATH);

        ZKConfig zkConfig = ConfigLoader.doParse(Config.class);

        Assert.assertEquals("localhost:2191", zkConfig.getZooKeeperConnection());
        Assert.assertFalse(zkConfig.isCacheHosts());
        Assert.assertEquals("", zkConfig.getZooKeeperBasePath());
    }

    @Test
    public void loadConfigFileWithAnyFieldsTest () {
        System.clearProperty(ConfigLoader.DEFAULT_CONFIG_ENV_PATH);
        TestLoadConfig testLoadConfig = ConfigLoader.doParse(TestLoadConfig.class, ConfigLoader.DEFAULT_NAME_FILE_PROPERTIES, null, null);

        Assert.assertEquals("Simple string for test!", testLoadConfig.getTestString());
        Assert.assertTrue(testLoadConfig.getTestBoolean());
        Assert.assertEquals(integer, testLoadConfig.getTestInteger());
        Assert.assertEquals(longNumber, testLoadConfig.getTestLong());
    }

    @Test
    public void loadConfigFileWithAnyFieldsAndPointTest () {
        System.clearProperty(ConfigLoader.DEFAULT_CONFIG_ENV_PATH);
        TestLoadConfig testLoadConfig = ConfigLoader.doParse(TestLoadConfig.class, ConfigLoader.DEFAULT_NAME_FILE_PROPERTIES, null, "root.");

        Assert.assertEquals("Simple string for test!", testLoadConfig.getTestString());
        Assert.assertTrue(testLoadConfig.getTestBoolean());
        Assert.assertEquals(integer, testLoadConfig.getTestInteger());
        Assert.assertEquals(longNumber, testLoadConfig.getTestLong());
    }

    @Test(expected = RuntimeException.class)
    public void loadConfigFileRuntimeExceptionTest () {
        System.clearProperty(ConfigLoader.DEFAULT_CONFIG_ENV_PATH);
        ConfigLoader.doParse(TestLoadConfigFailed.class);
    }

    @Test(expected = RuntimeException.class)
    public void loadConfigFileRuntimeExceptionTest0 () throws IOException {
        System.setProperty(ConfigLoader.DEFAULT_CONFIG_ENV_PATH, failPropertiesName);
        ConfigLoader.doParse(TestLoadConfig.class);
    }

    @Test(expected = RuntimeException.class)
    public void loadConfigFileWithStaticFieldsAndPointTest () {
        System.clearProperty(ConfigLoader.DEFAULT_CONFIG_ENV_PATH);
        ConfigLoader.doParse(TestLoadConfig.class, ConfigLoader.DEFAULT_NAME_FILE_PROPERTIES, "static.");
    }

}
