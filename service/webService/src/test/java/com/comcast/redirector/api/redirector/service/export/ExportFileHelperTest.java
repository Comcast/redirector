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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */


package com.comcast.redirector.api.redirector.service.export;

import com.comcast.redirector.dataaccess.EntityType;
import junit.framework.Assert;
import org.junit.Test;

public class ExportFileHelperTest {

    private final EntityType ENTITY_TYPE = EntityType.DISTRIBUTION;
    private final String ID = "id1";
    private final String SERVICE_NAME = "service1";

    @Test
    public void testCreateFileNameWith3Params () {
        ExportFileNameHelper helper = new ExportFileNameHelper();
        String name = helper.getFileNameForOneEntity(ENTITY_TYPE, SERVICE_NAME, ID);
        Assert.assertTrue(name.contains(ENTITY_TYPE.getPath()));
        Assert.assertTrue(name.contains(ID));
        Assert.assertTrue(name.contains(SERVICE_NAME));
    }

    @Test
    public void testCreateFileNameWith2Params () {
        ExportFileNameHelper helper = new ExportFileNameHelper();
        String name = helper.getFileNameForAll(ENTITY_TYPE, SERVICE_NAME);
        Assert.assertTrue(name.contains(ENTITY_TYPE.getPath()));
        Assert.assertFalse(name.contains(ID));
        Assert.assertTrue(name.contains(SERVICE_NAME));
    }

}
