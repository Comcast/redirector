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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigLoader {
    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);

    static final String DEFAULT_CONFIG_ROOT = "xre.redirectorConfig";
    static final String DEFAULT_CONFIG_ENV_PATH = "xre.include.site";
    static final String DEFAULT_NAME_FILE_PROPERTIES = "xre.properties";

    // TODO: rename to load?
    public static <T> T doParse (Class<T> clazz) {
        return doParse(clazz, DEFAULT_CONFIG_ENV_PATH, null, DEFAULT_CONFIG_ROOT);
    }

    public static <T> T doParse (Class<T> clazz, String filenameProperties) {
        File file = new File(filenameProperties);
        return doParse(clazz, null, file, DEFAULT_CONFIG_ROOT);
    }

    public static <T> T doParse (Class<T> clazz, String resourceName, String rootField) {
        return doParse(clazz, resourceName, null, rootField);
    }

    public static <T> T doParse (Class<T> clazz, String resourceName, File propertiesFileName, String rootField) {

        T object = null;

        try {
            object = clazz.newInstance();

            Field[] fields = object.getClass().getDeclaredFields();

            Map<String, String> lookup = null;

            if (propertiesFileName != null) {
                 lookup = getProperties(propertiesFileName);
            } else  {
                 lookup = getProperties(resourceName);
            }

            if (lookup != null && lookup.size() > 0) {

                for (Field field : fields) {
                    String name = field.getName();
                    String value = lookup.get(completeName(rootField, name));

                    if (value != null) {
                        field.setAccessible(true);
                        if (field.getType().equals(Boolean.TYPE) || field.getType().equals(Boolean.class)) {
                            field.set(object, Boolean.valueOf(value));
                        } else if (field.getType().equals(Integer.TYPE) || field.getType().equals(Integer.class)) {
                            field.set(object, Integer.valueOf(value));
                        } else if (field.getType().equals(Long.TYPE) || field.getType().equals(Long.class)) {
                            field.set(object, Long.valueOf(value));
                        } else {
                            field.set(object, value);
                        }
                    }
                }
            }

        } catch (IllegalAccessException e) {
            log.warn("Could not fill config Object - [{}], ", clazz.getName(), e.getMessage());
            throw new RuntimeException ("Could not fill config Object - [" + clazz.getName() + "]");
        } catch (InstantiationException e) {
            log.warn("Could not create config Object - [{}]", clazz.getName());
            throw new RuntimeException ("Could not fill config Object - [" + clazz.getName() + "]");
        }

        return object;
    }


    private static Map<String, String> getProperties (String resourceNameProperty) {

        try {
            Properties properties = PropertiesUtils.load(resourceNameProperty, DEFAULT_NAME_FILE_PROPERTIES);
            return new HashMap(properties);
        } catch (Exception e) {
            log.warn("Could not find properties file - [{}]", resourceNameProperty);
            throw new RuntimeException ("Couldn't load properties from " + resourceNameProperty);
        }
    }

    private static Map<String, String> getProperties (File filenameProperties) {

        try {
            Properties properties = PropertiesUtils.load(filenameProperties);
            return new HashMap(properties);
        } catch (Exception e) {
            log.warn("Could not find properties file - [{}]", filenameProperties);
            throw new RuntimeException ("Couldn't load properties from " + filenameProperties);
        }
    }

    private static String completeName(String rootField, String name) {
        if (rootField != null) {
            if (!rootField.endsWith(".")) {
                return rootField.concat(".").concat(name);
            } else {
                return rootField.concat(name);
            }
        } else {
            return name;
        }
    }
}
