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

package com.comcast.redirector.api.model;

import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.namespaced.Namespaces;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class NamespaceTest extends Assert {

    @Test
    public void testCREATE() throws Exception {


        NamespacedList nmsp = new NamespacedList();
        Set<Value> values  = new LinkedHashSet<>();
        values.add(new Value("test"));
        values.add(new Value("test1"));
        values.add(new Value("test2"));
        values.add(new Value("test3"));
        nmsp.setRet(values);
        List<NamespacedList> namespaceList = new ArrayList<NamespacedList>();
        namespaceList.add(nmsp);
        Namespaces mainNmsp = new Namespaces();
        mainNmsp.setNamespaces(namespaceList);
        final String result = serializeIt(mainNmsp, true);
        System.out.println(result);
        Assert.assertNotNull(result);
    }
    private String serializeIt(Namespaces rules, Boolean format) throws Exception {
        JAXBContext context = JAXBContext.newInstance(Namespaces.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, format);

        StringWriter result = new StringWriter();
        marshaller.marshal(rules, result);

        return result.toString();
    }
}
