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

package it.offline;

import com.comcast.xre.common.redirector.selector.xmlimpl.Config;
import it.context.TestContext;
import it.helper.IntegrationTestHelper;

class ModelFacadeIntegrationTestBase {
    private static final int ZK_PORT = 51829;
    static final String ZK_CONNECTION = "localhost:" + ZK_PORT;
    static final String ZK_BASE_PATH = "";
    static final int CONNECTION_TIMEOUT_MS = 1000;
    static final int SESSION_TIMEOUT_MS = 4000;
    static final int RETRY_COUNT = 2;
    static final int SLEEPS_BETWEEN_RETRY_MS = 100;
    static final boolean CACHE_HOSTS = true;
    static final int DISCOVERY_PULL_INTERVAL_MS = 300;
    static final int DISCOVERY_PULL_WORKERS_PULL_SIZE = 20;


    final IntegrationTestHelper.SimpleDataSourceBasedIntegrationHelperBuilder setupTestHelperBuilder(TestContext context) throws Exception {
        Config config = new Config();
        config.setZooKeeperBasePath(ZK_BASE_PATH);
        config.setZooKeeperConnection(ZK_CONNECTION);
        config.setZooKeeperConnectionTimeout(CONNECTION_TIMEOUT_MS);
        config.setZooKeeperSessionTimeout(SESSION_TIMEOUT_MS);
        config.setDiscoveryPullInterval(DISCOVERY_PULL_INTERVAL_MS);
        config.setDiscoveryPullWorkersPoolSize(DISCOVERY_PULL_WORKERS_PULL_SIZE);

        return new IntegrationTestHelper.Builder()
            .config(config)
            .context(context)
            .buildSimpleDataSourceBasedIntegrationHelper();
    }
}
