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
 * @author Alexey Smolenskiy (asmolenskiy@productengine.com)
 */

package com.comcast.redirector.api.model.xrestack;

import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.*;

public class ServicePathsTest {

    @Test
    public void testCreateObject() throws Exception {
        ServicePaths servicePaths = new ServicePaths();
        List<Paths> pathsList = new ArrayList<Paths>();
        for (int y = 0; y < 2; y++) {
            String serviceName = "serviceName" + y;
            List<PathItem> stacks = new ArrayList<PathItem>();
            List<PathItem> flavours = new ArrayList<PathItem>();
            for (int i = 0; i < 2; i++) {
                PathItem stacksPathItem = new PathItem();
                stacksPathItem.setActiveNodesCount(i);
                stacksPathItem.setValue("stackPathValue" + i);
                stacks.add(stacksPathItem);

                PathItem flavoursPathItem = new PathItem();
                flavoursPathItem.setActiveNodesCount(i);
                flavoursPathItem.setValue("flavoursPathValue" + i);
                flavours.add(flavoursPathItem);
            }
            Paths newPaths = new Paths(serviceName, stacks, flavours);
            pathsList.add(newPaths);
        }
        servicePaths.setPaths(pathsList);
        String result = serializeIt(servicePaths, false);
        Assert.assertNotNull(result);
        Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><servicePaths><paths serviceName=\"serviceName0\"><stack nodes=\"0\" nodesWhitelisted=\"0\">stackPathValue0</stack><stack nodes=\"1\" nodesWhitelisted=\"0\">stackPathValue1</stack><flavor nodes=\"0\" nodesWhitelisted=\"0\">flavoursPathValue0</flavor><flavor nodes=\"1\" nodesWhitelisted=\"0\">flavoursPathValue1</flavor></paths><paths serviceName=\"serviceName1\"><stack nodes=\"0\" nodesWhitelisted=\"0\">stackPathValue0</stack><stack nodes=\"1\" nodesWhitelisted=\"0\">stackPathValue1</stack><flavor nodes=\"0\" nodesWhitelisted=\"0\">flavoursPathValue0</flavor><flavor nodes=\"1\" nodesWhitelisted=\"0\">flavoursPathValue1</flavor></paths></servicePaths>",
                result);
    }

    private String serializeIt(ServicePaths servicePaths, Boolean format) throws Exception {
        JAXBContext context = JAXBContext.newInstance(ServicePaths.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, format);

        StringWriter result = new StringWriter();
        marshaller.marshal(servicePaths, result);

        return result.toString();
    }
}
