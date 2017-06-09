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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.common.serializers;

import com.comcast.redirector.common.util.AnnotationScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.annotation.Annotation;
import java.util.*;

import org.eclipse.persistence.jaxb.JAXBContextFactory;

public class JAXBContextBuilder {

    private static final Logger log = LoggerFactory.getLogger(JAXBContextBuilder.class);
    
    public JAXBContext createContextForXML() {
        return createContextForXML("com.comcast.redirector.api.model");
    }
    
    public JAXBContext createContextForJSON() {
        return createContextForJSON("com.comcast.redirector.api.model");
    }

    public JAXBContext createContextForXML(String... packages) {
        Class<? extends Annotation>[] modelClasses = new Class[]{XmlRootElement.class};
        Set<Class<?>> classes = AnnotationScanner.getAnnotatedClasses(modelClasses, packages);
        JAXBContext context;
        try {
            context = JAXBContext.newInstance(classes.toArray(new Class[classes.size()]));
        } catch (JAXBException e) {
            log.error("Failed to create JAXBContext", e);
            throw new IllegalStateException("JAXBContext should be created ", e.getCause());
        }
        return context;
    }
    
    public JAXBContext createContextForJSON(String... packages) {
        Class<? extends Annotation>[] modelClasses = new Class[]{XmlRootElement.class};
        Set<Class<?>> classes = AnnotationScanner.getAnnotatedClasses(modelClasses, packages);
        JAXBContext context;
        try {
            context = JAXBContextFactory.createContext(classes.toArray(new Class[classes.size()]), null);
        } catch (JAXBException e) {
            log.error("Failed to create JAXBContext", e);
            throw new IllegalStateException("JAXBContext should be created ", e.getCause());
        }
        return context;
    }
}
