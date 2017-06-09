/*
 * Copyright 2017 Comcast Cable Communications Management, LLC
 * <p>
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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.common.serializers;

import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

public class JSONSerializer implements Serializer {
    private static Logger log = LoggerFactory.getLogger(JSONSerializer.class);
    
    private JAXBContext context;
    
    public JSONSerializer() {
    }
    
    public JSONSerializer(JAXBContext context) {
        this.context = context;
    }
    
    public JAXBContext getContext() {
        return context;
    }
    
    public void setContext(JAXBContext context) {
        this.context = context;
    }
    
    @Override
    public byte[] serializeToByteArray(Object object) throws SerializerException {
        return serializeToByteArray(object, true);
    }
    
    @Override
    public String serialize(Object object) throws SerializerException {
        return serializeToByteArray(object, true).toString();
    }
    
    @Override
    public String serialize(Object object, boolean formatted) throws SerializerException {
        return serializeToByteArray(object, formatted).toString();
    }
    
    @Override
    public byte[] serializeToByteArray(Object object, boolean format) throws SerializerException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, format);
            marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
            marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false);
            marshaller.setProperty(MarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME, true);
            marshaller.marshal(object, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (JAXBException e) {
            log.error("Failed to serialize {}. {}", object.getClass().getCanonicalName(), e.getMessage());
            throw new SerializerException("Can't serialize object " + object, e);
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                log.error("Failed to close output stream while serializing {}. {}", object.getClass().getCanonicalName(), e.getMessage());
            }
        }
    }
    
    @Override
    public <T> T deserialize(String serialized, Class<T> clazz) throws SerializerException {
        try (StringReader reader = new StringReader(serialized)) {
            return deserializeInternal(new StreamSource(reader), clazz);
        }
    }
    
    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws SerializerException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        try {
            return deserializeInternal(new StreamSource(inputStream), clazz);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error("Failed to close input stream while deserializing {}. {}", clazz.getClass().getCanonicalName(), e.getMessage());
            }
        }
    }
    
    private <T> T deserializeInternal(StreamSource streamSource, Class<T> clazz) throws SerializerException {
        try {
            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
            unmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, false);
            unmarshaller.setProperty(UnmarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME, true);
            return unmarshaller.unmarshal(streamSource, clazz).getValue();
        } catch (JAXBException e) {
            log.error("Can't deserialize object of type {}", clazz.getSimpleName());
            throw new SerializerException("Can't deserialize object of type " + clazz.getSimpleName(), e);
        }
    }
}
