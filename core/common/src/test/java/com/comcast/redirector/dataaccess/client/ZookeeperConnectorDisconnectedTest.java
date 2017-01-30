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

package com.comcast.redirector.dataaccess.client;

import org.apache.curator.framework.state.ConnectionState;
import org.junit.Before;
import org.junit.Test;


public class ZookeeperConnectorDisconnectedTest extends ZookeeperConnectorTestBase {
    @Before
    public void setUp() throws Exception {
        setupClient();
        initZookeeperConnector();
    }

    @Test(expected = NoConnectionToDataSourceException.class)
    public void testSaveInTransactionDisconnected() throws Exception {
        setupDisconnect();

        testee.createTransaction().save("success", "/path");
    }

    @Test(expected = NoConnectionToDataSourceException.class)
    public void testDeleteDisconnected() throws Exception {
        setupDisconnect();

        testee.delete("/path");
    }

    @Test(expected = NoConnectionToDataSourceException.class)
    public void testSaveDisconnected() throws Exception {
        setupDisconnect();

        testee.saveCompressed("success", "/path");
    }

    @Test(expected = NoConnectionToDataSourceException.class)
    public void testGetDataDisconnected() throws Exception {
        setupDisconnect();

        testee.getData("/test/NodeA");
    }

    @Test(expected = NoConnectionToDataSourceException.class)
    public void testGetChildrenDisconnected() throws Exception {
        setupDisconnect();

        testee.getChildren("/test/NodeA");
    }

    @Test(expected = NoConnectionToDataSourceException.class)
    public void testIsZNodeExistsDisconnected() throws Exception {
        setupDisconnect();

        testee.isPathExists("/test/NodeA");
    }

    private void setupDisconnect() {
        testee.stateChanged(client, ConnectionState.LOST);
    }
}
