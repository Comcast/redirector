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

package com.comcast.redirector.core.modelupdate.holder;

import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.dataaccess.facade.ICommonModelFacade;
import com.comcast.redirector.ruleengine.model.IpAddress;
import com.comcast.redirector.ruleengine.IpAddressInitException;
import com.comcast.redirector.core.modelupdate.helper.NamespacedListsHelper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class NamespacedListHolderIpAddressesCachingTest {
    private ICommonModelFacade commonModelFacade;
    private NamespacedListsHolder namespacedListsHolder;

    private NamespacedList ipRange1 = NamespacedListsHelper.createNamespacedList("IpRange1", "123.123.123.0/24", "100.100.100.0/25");
    private NamespacedList ipRange2 = NamespacedListsHelper.createNamespacedList("IpRange2", "223.123.123.0/24", "200.100.100.0/25");
    private NamespacedList macList = NamespacedListsHelper.createNamespacedList("macList", "mac1", "mac2");

    @Before
    public void setUp() throws Exception {
        namespacedListsHolder = NamespacedListsHelper.prepareNamespacedListsHolderBeforeTest();

        commonModelFacade = mock(ICommonModelFacade.class);
        when(commonModelFacade.getAllNamespacedLists()).thenReturn( Arrays.asList(ipRange1, ipRange2, macList) );
        ReflectionTestUtils.setField(namespacedListsHolder, "commonModelFacade", commonModelFacade);
    }

    @Test
    public void getIpAddressesFromNamespacedListCachesIpAddresses() throws Exception {
        namespacedListsHolder.load(IModelHolder.GET_FROM_DATA_STORE);

        Set<IpAddress> resultRange1 = namespacedListsHolder.getIpAddressesFromNamespacedList(ipRange1.getName());

        Set<IpAddress> cachedIpRange1 = namespacedListsHolder.getIpAddressesFromCacheForNamespacedList(ipRange1.getName());
        Set<IpAddress> cachedIpRange2 = namespacedListsHolder.getIpAddressesFromCacheForNamespacedList(ipRange2.getName());
        verifyNamespacedListContainsAllGivenIpAddresses(ipRange1, resultRange1);
        verifyNamespacedListContainsAllGivenIpAddresses(ipRange1, cachedIpRange1);
        assertTrue(cachedIpRange2.isEmpty());

        Set<IpAddress> resultRange2 = namespacedListsHolder.getIpAddressesFromNamespacedList(ipRange2.getName());

        cachedIpRange1 = namespacedListsHolder.getIpAddressesFromCacheForNamespacedList(ipRange1.getName());
        cachedIpRange2 = namespacedListsHolder.getIpAddressesFromCacheForNamespacedList(ipRange2.getName());
        verifyNamespacedListContainsAllGivenIpAddresses(ipRange2, resultRange2);
        verifyNamespacedListContainsAllGivenIpAddresses(ipRange2, cachedIpRange2);
        verifyNamespacedListContainsAllGivenIpAddresses(ipRange1, cachedIpRange1);

        assertTrue(namespacedListsHolder.getIpAddressesFromNamespacedList(macList.getName()).isEmpty());
    }

    private void verifyNamespacedListContainsAllGivenIpAddresses(NamespacedList list, Set<IpAddress> ipAddresses)
        throws IpAddressInitException {

        for (String value : list.getValues()) {
            assertTrue(ipAddresses.contains(new IpAddress(value)));
        }
    }

    @Test
    public void loadFromDataStoreResetsCacheOfIpAddresses() throws Exception {
        namespacedListsHolder.load(IModelHolder.GET_FROM_DATA_STORE);

        namespacedListsHolder.getIpAddressesFromNamespacedList(ipRange1.getName());
        Set<IpAddress> cachedIpRange1 = namespacedListsHolder.getIpAddressesFromCacheForNamespacedList(ipRange1.getName());
        verifyNamespacedListContainsAllGivenIpAddresses(ipRange1, cachedIpRange1);

        namespacedListsHolder.load(IModelHolder.GET_FROM_DATA_STORE);
        Set<IpAddress> cache = namespacedListsHolder.getIpAddressesFromCacheForNamespacedList(ipRange1.getName());
        assertTrue(cache.isEmpty());
    }
}
