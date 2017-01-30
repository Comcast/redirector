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
 * @author Stanislav Menshykov (smenshykov@productengine.com)
 */

package com.comcast.apps.e2e.runners;

import com.comcast.apps.e2e.config.E2EConfigLoader;
import com.comcast.apps.e2e.helpers.FilesPathHelper;
import com.comcast.apps.e2e.helpers.ServiceHelper;
import com.comcast.redirector.api.model.Value;
import com.comcast.redirector.api.model.testsuite.Parameter;
import com.comcast.redirector.api.model.testsuite.RedirectorTestCase;
import com.comcast.redirector.api.model.testsuite.RedirectorTestCaseList;
import com.comcast.redirector.common.Context;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.endpoint.http.Constants;
import com.comcast.xre.common.redirector.DataParamName;
import org.apache.commons.collections.CollectionUtils;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import java.util.List;

public class HttpTestCasesRunner implements ITestCasesRunner {

    @Override
    public Boolean performTestCases(RedirectorTestCaseList redirectorTestCaseList, String appName, FilesPathHelper filesPathHelper) {
        for (RedirectorTestCase redirectorTestCase : redirectorTestCaseList.getRedirectorTestCases()) {
            Invocation.Builder request = getRequestForTestCase(redirectorTestCase, appName);
            request.get();
        }

        return true;
    }

    private Invocation.Builder getRequestForTestCase(RedirectorTestCase redirectorTestCase, String appName) {
        WebTarget webTarget = new ServiceHelper(endpointUrlForApp(appName)).getWebTarget();
        webTarget = webTarget.queryParam(DataParamName.testInfo.name(), redirectorTestCase.getName());
        webTarget = webTarget.queryParam(Context.SESSION_ID, redirectorTestCase.getName());
        for (Parameter parameter : redirectorTestCase.getParameters()) {
            List<Value> values = parameter.getValues();
            Value value = CollectionUtils.isNotEmpty(values) ? values.get(0) : new Value("");
            webTarget = webTarget.queryParam(parameter.getName(), value);
        }

        return webTarget.request();
    }

    private String endpointUrlForApp(String appName) {
        return E2EConfigLoader.getDefaultInstance().getRedirectorEndpoint() + RedirectorConstants.DELIMETER + Constants.SERVICE_URL_PREFIX + RedirectorConstants.DELIMETER + appName;
    }
}
