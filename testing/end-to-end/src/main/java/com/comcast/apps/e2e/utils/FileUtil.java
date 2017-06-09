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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.apps.e2e.utils;

import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.SerializerException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {
    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);

    public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    private final static boolean IS_WINDOWS = System.getProperty("os.name").contains("indow");
    public final static String FILE_SEPARATOR = "/";

    private final Serializer jsonSerializer;
    private final Serializer xmlSerializer;

    public FileUtil(Serializer jsonSerializer, Serializer xmlSerializer) {
        this.jsonSerializer = jsonSerializer;
        this.xmlSerializer = xmlSerializer;
    }

    public void writeJson(String fileName, Object object) throws IOException, SerializerException {
        Path path = Paths.get(fileName);
        String jsonObject = jsonSerializer.serialize(object);
        byte[] bytes = jsonObject.getBytes(UTF8_CHARSET);
        Files.write(path, bytes);
    }

    public void writeXML(String fileName, Object object) throws SerializerException, IOException {
        Path path = Paths.get(fileName);
        String xmlObject = xmlSerializer.serialize(object);
        byte[] bytes = xmlObject.getBytes(UTF8_CHARSET);
        Files.write(path, bytes);
    }

    public <T> T readJson(String fileName, Class<T> entityClassType) {
        String result = load(fileName);
        try {
            return (result == null) ? null : jsonSerializer.deserialize(result, entityClassType);
        } catch (SerializerException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public <T> T readXML(String fileName, Class<T> entityClassType) {
        String result = load(fileName);
        try {
            return (result == null) ? null : xmlSerializer.deserialize(result, entityClassType);
        } catch (SerializerException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public static String getFilePath(String fileName) {
        ClassLoader loader = FileUtil.class.getClassLoader();
        URL url = loader.getResource(fileName);
        if (url == null) {
            String messageError = String.format("File with filename: %s doesn't exist", fileName);
            log.error(messageError);
            return "";
        }
        String filePath = url.getPath();
        return  IS_WINDOWS ? filePath.substring(1) : filePath;
    }

    public String load(String fileName) {
        String result = null;
        try {
            Path path = Paths.get(fileName);
            if (Files.exists(path)) {
                byte[] bytes = Files.readAllBytes(path);
                result = new String(bytes, "UTF-8");
                if (StringUtils.isBlank(result)) {
                    log.warn("Failed to read file {}: data is absent", fileName);
                }
            } else {
                log.warn("Failed to read file {}: file is absent", fileName);
            }
        } catch (IOException e) {
            log.error("Failed to read data from file: {}", e.getMessage());
        }
        return result;
    }

    public void delete(String fileName) throws IOException {
        Files.deleteIfExists(Paths.get(fileName));
    }
}
