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

package com.comcast.redirector.core.modelupdate.chain;

import com.comcast.redirector.api.model.xrestack.StackData;
import com.comcast.redirector.core.engine.IRedirectorEngineFactory;
import com.comcast.redirector.ruleengine.model.Model;
import com.comcast.redirector.ruleengine.model.URLRuleModel;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackBackup;
import com.comcast.redirector.core.balancer.serviceprovider.whitelist.WhiteList;
import com.comcast.redirector.core.engine.IRedirectorEngine;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class ApplyNewModelTaskTest {
    private static final String APP_NAME = "appname";

    private ModelContext modelContext;
    private IRedirectorEngineFactory redirectorEngineFactory;

    private ApplyNewModelTask task;

    @Before
    public void setUp() throws Exception {
        redirectorEngineFactory = mock(IRedirectorEngineFactory.class);
        when(redirectorEngineFactory.newRedirectorEngine(anyString(), any(Model.class), any(URLRuleModel.class), any(WhiteList.class), anySetOf(StackData.class), anyInt()))
                .thenReturn(mock(IRedirectorEngine.class));

        modelContext = new ModelContext();
        modelContext.setAppName(APP_NAME);
        modelContext.setFlavorRulesModel(mock(Model.class));
        modelContext.setUrlRulesModel(mock(URLRuleModel.class));
        modelContext.setWhiteListModel(mock(WhiteList.class));
        modelContext.setMainStacksBackup(new StackBackup());

        task = new ApplyNewModelTask(true, redirectorEngineFactory);
    }

    @Test
    public void testSuccessfulApply() throws Exception {
        verifySuccess();
    }

    @Test
    public void testNullFlavorRules() throws Exception {
        modelContext.setFlavorRulesModel(null);
        verifyFailed();
    }

    @Test
    public void testNullUrlRules() throws Exception {
        modelContext.setUrlRulesModel(null);
        verifyFailed();
    }

    @Test
    public void testNullWhiteList() throws Exception {
        modelContext.setWhiteListModel(null);
        verifyFailed();
    }

    private void verifySuccess() {
        Assert.assertTrue(task.handle(modelContext).isSuccessful());
        Assert.assertNotNull(modelContext.getRedirectorEngine());
        verify(redirectorEngineFactory, times(1))
                .newRedirectorEngine(eq(APP_NAME), any(Model.class), any(URLRuleModel.class), any(WhiteList.class), anySetOf(StackData.class), anyInt());
    }

    private void verifyFailed() {
        Assert.assertFalse(task.handle(modelContext).isSuccessful());
        Assert.assertNull(modelContext.getRedirectorEngine());
        verify(redirectorEngineFactory, never())
                .newRedirectorEngine(eq(APP_NAME), any(Model.class), any(URLRuleModel.class), any(WhiteList.class), anySetOf(StackData.class), anyInt());
    }
}
