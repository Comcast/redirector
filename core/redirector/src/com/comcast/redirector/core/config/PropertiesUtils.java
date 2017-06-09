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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

class PropertiesUtils {
    private static final Logger log = LoggerFactory.getLogger(PropertiesUtils.class);

    private PropertiesUtils() {
    }

    public static Properties load(String resourceNameProperty, String defaultPropertiesFileName) throws Exception {

        String customPropertiesName = System.getProperty(resourceNameProperty);

        URL urlResource = null;

        if (StringUtils.isNotBlank(customPropertiesName)) {
            urlResource = ClassLoader.getSystemResource(customPropertiesName);
        } else {
            urlResource = ClassLoader.getSystemResource(resourceNameProperty);
            if (urlResource == null) {
                urlResource = ClassLoader.getSystemResource(defaultPropertiesFileName);
            }
        }
        Properties properties = new Properties();
        properties.load(urlResource.openStream());
        return properties;
    }

    public static Properties load(File propertiesFileName) {
        Properties properties = new Properties();
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(propertiesFileName);
            properties.load(fileInputStream);
        } catch (IOException e) {
            log.error("Could not read file properties: {}", propertiesFileName);
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                log.error("Could not close file properties: {}", propertiesFileName);
            }
        }
        return properties;
    }
}
