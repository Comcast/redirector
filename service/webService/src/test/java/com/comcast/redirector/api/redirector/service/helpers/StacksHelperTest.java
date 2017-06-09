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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.redirector.api.redirector.service.helpers;


import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.xrestack.HostIPs;
import com.comcast.redirector.api.model.xrestack.StackData;
import com.comcast.redirector.common.util.StacksHelper;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class StacksHelperTest {

    private final static String FULL_PATH_STACK = "/DataCenter1/Region1/Zone12/serviceName";
    private final static String PATH_STACK_WITHOUT_SERVICE_NAME = "/DataCenter1/Region1/Zone12";
    private final static String FLAVOR_PATH_NAME = "Zone12";
    private final static String STACK_PATH_NAME = "/DataCenter1/Region1";

    @Test
    public void testGetFlavorPath() {
        String actualFlavorName = StacksHelper.getFlavorPath(PATH_STACK_WITHOUT_SERVICE_NAME);
        assertEquals(FLAVOR_PATH_NAME, actualFlavorName);
    }

    @Test
    public void testGetStackPath() {
        String actualStackName = StacksHelper.getStackPath(PATH_STACK_WITHOUT_SERVICE_NAME);
        assertEquals(STACK_PATH_NAME, actualStackName);
    }

    @Test
    public void testIsWhitelistedStackIfWhitelistedDoesNotHaveWhitelistedStacks() {
        Whitelisted whitelisted = new Whitelisted();
        boolean actualResult = StacksHelper.isWhitelistedStack(createStackData(), whitelisted);
        assertEquals(false, actualResult);
    }

    @Test
    public void testIsWhitelistedStackIfWhitelistedHaveWhitelistedStacks() {
        Whitelisted whitelisted = createWhitelisted();
        boolean actualResult = StacksHelper.isWhitelistedStack(createStackData(), whitelisted);
        assertEquals(true, actualResult);
    }

    @Test
    public void testIsActiveAndWhitelistedHostsForFlavorIfWhitelistedDoesNotHaveWhitelistedStacks() {
        Whitelisted whitelisted = new Whitelisted();
        Set<StackData> stackDataSet = new HashSet<>();
        stackDataSet.add(createStackData());
        boolean actualResult = StacksHelper.isActiveAndWhitelistedHostsForFlavor(FLAVOR_PATH_NAME, stackDataSet, whitelisted);
        assertEquals(false, actualResult);
    }

    @Test
    public void testIsActiveAndWhitelistedHostsForFlavorIfWhitelistedHaveWhitelistedStacks() {
        Whitelisted whitelisted = createWhitelisted();
        Set<StackData> stackDataSet = new HashSet<>();
        stackDataSet.add(createStackData());
        boolean actualResult = StacksHelper.isActiveAndWhitelistedHostsForFlavor(FLAVOR_PATH_NAME, stackDataSet, whitelisted);
        assertEquals(true, actualResult);
    }

    private StackData createStackData() {
        List<HostIPs> hostIPsList = new ArrayList<>();
        HostIPs hostIPs = new HostIPs("host1ipv4", "host1ipv6");
        hostIPsList.add(hostIPs);
        StackData stackData = new StackData(FULL_PATH_STACK, hostIPsList);
        return stackData;
    }

    private Whitelisted createWhitelisted() {
        Whitelisted whitelisted = new Whitelisted();
        List<String> paths = new ArrayList<>();
        paths.add(STACK_PATH_NAME);
        whitelisted.setPaths(paths);
        return whitelisted;
    }
}
