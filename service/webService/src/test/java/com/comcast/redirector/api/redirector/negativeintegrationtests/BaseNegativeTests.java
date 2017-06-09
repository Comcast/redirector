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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.api.redirector.negativeintegrationtests;

import com.comcast.redirector.api.model.builders.DistributionBuilder;
import com.comcast.redirector.api.redirector.helpers.*;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.api.model.RedirectorConfig;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.validation.ValidationState;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.xrestack.ServicePaths;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

import static com.comcast.redirector.api.model.factory.ServerFactory.newSimpleServerForFlavor;
import static com.comcast.redirector.api.model.factory.ServicePathsFactory.newServicePaths;

public class BaseNegativeTests {

    @Autowired
    protected IDataSourceConnector zClient;

    protected static final String SETTINGS_SERVICE_PATH = RedirectorConstants.SETTINGS;
    protected static final String RULES_SERVICE_PATH = RedirectorConstants.RULES_CONTROLLER_PATH;
    protected static final String SERVERS_SERVICE_PATH = RedirectorConstants.SERVERS_CONTROLLER_PATH;
    protected static final String URL_RULES_SERVICE_PATH = RedirectorConstants.URL_RULES_CONTROLLER_PATH;
    protected static final String NAMESPACE_SERVICE_PATH = RedirectorConstants.NAMESPACE_CONTROLLER_PATH;
    protected static final String WHITELISTED_SERVICE_PATH = RedirectorConstants.WHITELISTED_CONTROLLER_PATH;
    protected static final String DISTRIBUTION_SERVICE_PATH = RedirectorConstants.DISTRIBUTION_CONTROLLER_PATH;
    protected static final String TEMPLATES_RULE_SERVICE_PATH = RedirectorConstants.TEMPLATES_RULE_CONTROLLER_PATH;
    protected static final String SERVICE_NAME = "TEST_APP";
    protected static final String STACK0 = "/PO/POC1/1.0";
    protected static final String STACK1 = "/PO/POC1/1.1";
    protected static final String STACK2 = "/PO/POC1/1.2";
    protected static final String STACK3 = "/PO/POC1/1.3";
    protected static final String STACK4 = "/PO/POC2/1.4";
    protected static final String STACK5 = "/PO/POC5/1.5";
    protected static final String STACK6 = "/PO/POC5/1.6";

    protected static Server defaultServer;

    public BaseNegativeTests() {
        this.defaultServer = newSimpleServerForFlavor("1.0");
    }

    @Before
    public void init() throws InterruptedException {

        Map<String, Integer> paths = new HashMap<>();
        paths.put(STACK0, 1);
        paths.put(STACK1, 1);
        paths.put(STACK2, 1);
        paths.put(STACK3, 1);
        paths.put(STACK4, 1);
        paths.put(STACK5, 1); // active but not whitelisted
        paths.put(STACK6, 0); // whitelisted but not active

        // save service path with active stacks first
        ServicePaths servicePaths = newServicePaths(SERVICE_NAME, paths);
        StacksHelper.zkPostStacks(zClient, servicePaths);

        // make stack whitelisted
        Whitelisted whitelisted = WhitelistedHelper.createWhitelisted(STACK1.substring(0, STACK1.lastIndexOf("/")), STACK5.substring(0, STACK5.lastIndexOf("/")));
        WhitelistedHelper.postAndApprove(HttpTestServerHelper.target(), SERVICE_NAME, whitelisted);

        RedirectorConfig redirectorConfig = new RedirectorConfig(0, 0);
        ServiceHelper.post(HttpTestServerHelper.target().path(SETTINGS_SERVICE_PATH).path("redirectorConfig"), redirectorConfig, MediaType.APPLICATION_JSON);

        Thread.sleep(1000);
        ServiceHelper.post(HttpTestServerHelper.target().path(SERVERS_SERVICE_PATH).path(SERVICE_NAME).path(RedirectorConstants.DEFAULT_SERVER_NAME), defaultServer, MediaType.APPLICATION_JSON);

        DistributionBuilder builder = new DistributionBuilder();
        Distribution distribution = builder.withDefaultServer(defaultServer).build();
        WebTarget webTarget = HttpTestServerHelper.target().path(DISTRIBUTION_SERVICE_PATH).path(SERVICE_NAME);
        ServiceHelper.post(webTarget, distribution, MediaType.APPLICATION_JSON, ValidationState.class);
        DistributionHelper.approvePendingChanges(HttpTestServerHelper.target(), SERVICE_NAME);
    }
}
