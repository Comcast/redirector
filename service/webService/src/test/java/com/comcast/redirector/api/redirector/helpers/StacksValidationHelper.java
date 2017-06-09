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
package com.comcast.redirector.api.redirector.helpers;

import com.comcast.redirector.api.model.xrestack.PathItem;
import com.comcast.redirector.api.model.xrestack.Paths;
import com.comcast.redirector.api.model.xrestack.ServicePaths;
import com.comcast.redirector.common.serializers.JAXBContextBuilder;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.common.serializers.XMLSerializer;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class StacksValidationHelper {
    private static Serializer xmlSerializer = new XMLSerializer(new JAXBContextBuilder().createContextForXML());

    public static void validatePathsInResponse(Paths expectedPaths, ServicePaths pathsResponseEntityObject) throws AssertionError, SerializerException {
        // validate received objectHttpTestServerHelper.target()
        assertNotNull(pathsResponseEntityObject);
        assertEquals(1, pathsResponseEntityObject.getPaths().size());
        try {
            validatePaths(expectedPaths, pathsResponseEntityObject.getPaths().get(0));
        } catch (AssertionError ae) {
            fail("\nReceived Paths object doesn't match with needed one: "
                    + "\nAssert message: " + ae.getMessage()
                    + "\nExpected Paths object: \n" + xmlSerializer.serialize(expectedPaths, true)
                    + "\nReceived Paths object: \n" + xmlSerializer.serialize(pathsResponseEntityObject.getPaths().get(0), true));
        }
    }

    public static void validatePaths(Paths expectedPathsWithFlavors, Paths receivedPaths)
            throws AssertionError {
        assertEquals(expectedPathsWithFlavors.getServiceName(), receivedPaths.getServiceName());
        assertEquals("Stacks size assertion error:",
                expectedPathsWithFlavors.getStacks().size(), receivedPaths.getStacks().size());
        assertEquals("Flavors size assertion error:",
                expectedPathsWithFlavors.getFlavors().size(), receivedPaths.getFlavors().size());
        validatePathItemLists(expectedPathsWithFlavors.getStacks(), receivedPaths.getStacks());
        validatePathItemLists(expectedPathsWithFlavors.getFlavors(), receivedPaths.getFlavors());
    }

    private static void validatePathItemLists(List<PathItem> expectedPathItemList, List<PathItem> actualPathItemList)
            throws AssertionError {
        for (PathItem expectedPathItem : expectedPathItemList) {
            boolean pathItemFound = false;
            for (PathItem actualPathItem : actualPathItemList) {
                if (expectedPathItem.getValue().equals(actualPathItem.getValue())) {
                    pathItemFound = true;
                    assertEquals("ActiveNodesCount assertion error for " + expectedPathItem.getValue() + "PathItem:",
                            expectedPathItem.getActiveNodesCount(), actualPathItem.getActiveNodesCount());
                    break;
                }
            }
            if (!pathItemFound) {
                fail("PathItem (" + expectedPathItem.getValue() + " ) not found!");
            }
        }
    }
}
