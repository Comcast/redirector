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

package it.offline;

import com.comcast.redirector.api.model.xrestack.HostIPs;
import com.comcast.redirector.api.model.xrestack.XreStackPath;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.client.ZookeeperConnector;
import com.comcast.redirector.dataaccess.dao.DAOFactory;
import com.comcast.redirector.dataaccess.dao.IStacksDAO;
import it.context.ContextBuilder;
import it.context.TestContext;
import it.helper.IntegrationTestHelper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.junit.*;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static com.comcast.redirector.common.RedirectorConstants.DELIMETER;

public class StacksDaoCacheIntegrationTest extends ModelFacadeIntegrationTestBase {
    private static final String DC = "offlinemode";
    private static final String STACK = "stack";
    private static final String WHITELISTED_STACK = DELIMETER + DC + DELIMETER + STACK;
    private static final int DISCOVERY_PULL_INTERVAL_MS = 300;

    private IStacksDAO testee;
    private IDataSourceConnector connector;

    @Before
    public void before() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
            .connectionTimeoutMs(CONNECTION_TIMEOUT_MS)
            .retryPolicy(new RetryNTimes(RETRY_COUNT, SLEEPS_BETWEEN_RETRY_MS))
            .connectString(ZK_CONNECTION).build();
        connector = new ZookeeperConnector(curatorFramework, ZK_BASE_PATH, CACHE_HOSTS);

        connector.connect();
        testee = new DAOFactory(connector, null).createStacksDAO();
    }

    @After
    public void after() throws Exception {
        connector.disconnect();
    }

    @Ignore
    @Test
    public void hosts_AreCompletelyCached_AndReturnedFromCache_WhenDataSourceGoesDown() throws Exception {
        TestContext context = new ContextBuilder().withHosts()
            .stack(WHITELISTED_STACK).flavor("flavor1").ipv4("10.0.0.1").ipv6("ff01::41").app("test")
            .stack(WHITELISTED_STACK).flavor("flavor2").ipv4("10.0.0.2").ipv6("ff01::42").app("otherApp")
            .stack(WHITELISTED_STACK).flavor("flavor2").ipv4("10.0.0.4").ipv6("ff01::44").app("newApp")
            .build();

        IntegrationTestHelper helper = setupTestHelperBuilder(context).startDataStore();

        helper.stopDataStore();

        TimeUnit.MILLISECONDS.sleep(SESSION_TIMEOUT_MS);
        Collection<XreStackPath> paths = testee.getAllStackPaths();
        Collection<HostIPs> hosts = testee.getHosts(paths.iterator().next());
        Assert.assertNotNull(paths);
        Assert.assertNotNull(hosts);
    }
}
