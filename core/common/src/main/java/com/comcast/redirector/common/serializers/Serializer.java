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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package com.comcast.redirector.common.serializers;

public interface Serializer {
    String serialize(Object object) throws SerializerException;
    String serialize(Object object, boolean formatted) throws SerializerException;
    byte[] serializeToByteArray(Object object, boolean format) throws SerializerException;
    byte[] serializeToByteArray(Object object) throws SerializerException;
    <T> T deserialize(String serialized, Class<T> clazz) throws SerializerException;
    <T> T deserialize(byte[] data, Class<T> clazz) throws SerializerException;
}
