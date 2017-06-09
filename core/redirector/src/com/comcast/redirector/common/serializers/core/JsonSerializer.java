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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.common.serializers.core;

import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.SerializerException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonSerializer implements Serializer {
    private ObjectMapper mapper = new ObjectMapper();

    public JsonSerializer() {

    }

    public JsonSerializer(boolean nonNullInclude) {
        if (nonNullInclude) {
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }
    }

    @Override
    public String serialize(Object object) {
        return serialize(object, true);
    }

    @Override
    public String serialize(Object object, boolean formatted) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Can't serialize: \"" + object + "\"", e);
        }
    }
    
    @Override
    public byte[] serializeToByteArray(Object object, boolean format) throws SerializerException {
        return serialize(object, format).getBytes();
    }
    
    @Override
    public byte[] serializeToByteArray(Object object) throws SerializerException {
        return serialize(object, true).getBytes();
    }
    
    @Override
    public <T> T deserialize(String serialized, Class<T> clazz) {
        try {
            return mapper.readValue(serialized, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Can't deserialize: \"" + serialized + "\"", e);
        }
    }
    
    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws SerializerException {
        return deserialize(new String(data), clazz);
    }
    
    public static JsonSerializer serializerIncludeNonNull() {
        return new JsonSerializer(true);
    }
}
