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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.dataaccess.client;

import com.comcast.redirector.dataaccess.cache.IStacksCache;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.listen.Listenable;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class SimpleZookeeperConnectorTest {

    private CuratorFramework curator;
    private Listenable connectionListenable;

    @Before
    public void before() {
        curator = mock(CuratorFramework.class);
        connectionListenable = mock(Listenable.class);

        when(curator.getConnectionStateListenable()).thenReturn(connectionListenable);
    }

    @Test
    public void connectWithExistingCurator() throws Exception {
        ZookeeperConnector testee = new ZookeeperConnector(curator, "/anyBasePath", true, () -> mock(IStacksCache.class));

        testee.connect();

        verifyClientConnected(testee);
    }

    private void verifyClientConnected(ZookeeperConnector testee) {
        verify(curator, times(1)).start();
        verify(connectionListenable, times(1)).addListener(eq(testee));
    }
}
