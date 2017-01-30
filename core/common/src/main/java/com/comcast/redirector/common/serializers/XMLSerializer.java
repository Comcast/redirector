/**
 * Copyright 2016 Comcast Cable Communications Management, LLC 
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;

public class XMLSerializer implements Serializer {
    private static Logger log = LoggerFactory.getLogger(XMLSerializer.class);

    private JAXBContext context;

    public XMLSerializer() {
    }

    public XMLSerializer(JAXBContext context) {
        this.context = context;
    }

    @Override
    public String serialize(Object object) throws SerializerException {
        return serialize(object, true);
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
    public String serialize(Object object, boolean format) throws SerializerException {
        try {
            StringWriter stringWriter = new StringWriter();
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, format);
            marshaller.marshal(object, stringWriter);
            String marshalled = stringWriter.toString();

            //reflexivity check, because JAXB may marshal to corrupted xml.
            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.unmarshal(new StringReader(marshalled));
            return marshalled;
        } catch (JAXBException e) {
            throw new SerializerException("Can't serialize object " + object.toString(), e);
        }
    }
   
    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(String serialized, Class<T> clazz) throws SerializerException {
        try {
            Unmarshaller unmarshaller = context.createUnmarshaller();
            StringReader reader = new StringReader(serialized);
            return (T)unmarshaller.unmarshal(reader);
        } catch (JAXBException e) {
            log.error("Can't deserialize object of type {}", clazz.getSimpleName());
            throw new SerializerException("Can't deserialize object of type " + clazz.getSimpleName(), e);
        }
    }
    
    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws SerializerException {
        return deserialize(new String(data), clazz);
    }
    
    public JAXBContext getContext() {
        return context;
    }

    public void setContext(JAXBContext context) {
        this.context = context;
    }
}
