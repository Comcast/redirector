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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.dataaccess;

import junit.framework.Assert;
import org.junit.Test;

public class PathHelperTest {
    @Test
    public void testGetRawPath() throws Exception {
        IPathHelper helper = PathHelper.getPathHelper(EntityType.STACK, "/test");

        Assert.assertEquals("/test/services/PO/POC7/1.45/xreGuide", helper.getRawPath("/PO/POC7/1.45", "xreGuide"));
    }

    @Test
    public void testGetPathForStacks() throws Exception {
        IPathHelper helper = PathHelper.getPathHelper(EntityType.STACK, "/test");

        Assert.assertEquals("/test/services", helper.getPath());
    }

    @Test
    public void testGetPathByService() throws Exception {
        IPathHelper helper = PathHelper.getPathHelper(EntityType.RULE, "/test");

        Assert.assertEquals("/test/Redirector/services/xreGuide/rules", helper.getPathByService("xreGuide"));
    }

    @Test
    public void testGetPathByServiceAndId() throws Exception {
        IPathHelper helper = PathHelper.getPathHelper(EntityType.RULE, "/test");

        Assert.assertEquals("/test/Redirector/services/xreGuide/rules/testId", helper.getPathByService("xreGuide", "testId"));
    }

    @Test
    public void testGetPathByServiceEntityTypeAndId() throws Exception {
        IPathHelper helper = PathHelper.getPathHelper(EntityCategory.GLOBAL, "/test");

        Assert.assertEquals("/test/xreGuide/testCases/testId",
                helper.getPathByService("xreGuide", EntityType.TEST_CASE, "testId"));
    }

    @Test
    public void testGetPathByEntityTypeAndId() throws Exception {
        IPathHelper helper = PathHelper.getPathHelper(EntityCategory.GLOBAL, "/test");

        Assert.assertEquals("/test/namespacedList/testId",
                helper.getPath(EntityType.NAMESPACED_LIST, "testId"));
    }

    @Test
    public void testGetPathByEntityType() throws Exception {
        IPathHelper helper = PathHelper.getPathHelper(EntityCategory.GLOBAL, "/test");

        Assert.assertEquals("/test/namespacedList",
                helper.getPath(EntityType.NAMESPACED_LIST));
    }

    @Test
    public void testGetPathById() throws Exception {
        IPathHelper helper = PathHelper.getPathHelper(EntityType.NAMESPACED_LIST, "/test");

        Assert.assertEquals("/test/namespacedList/test1", helper.getPath("test1"));
    }
}
