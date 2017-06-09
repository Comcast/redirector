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

package com.comcast.redirector.core.modelupdate.chain;

import com.comcast.redirector.api.model.url.rule.Default;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.core.modelupdate.converter.ModelTranslationService;
import com.comcast.redirector.core.modelupdate.holder.IModelHolder;
import com.comcast.redirector.ruleengine.model.URLRuleModel;
import com.comcast.redirector.ruleengine.repository.NamespacedListRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.*;

public class GetUrlRulesTaskTest {
    private IModelHolder<URLRules> urlRulesHolder;
    private NamespacedListRepository namespacedListsHolder;
    private ModelTranslationService modelTranslationService;

    @Before
    public void setUp() throws Exception {
        urlRulesHolder = (IModelHolder<URLRules>)mock(IModelHolder.class);
        when(urlRulesHolder.load(anyBoolean())).thenReturn(newValidModel());

        namespacedListsHolder = mock(NamespacedListRepository.class);

        modelTranslationService = mock(ModelTranslationService.class);
        when(modelTranslationService.translateUrlRules(any(URLRules.class), any(NamespacedListRepository.class)))
            .thenReturn(mock(URLRuleModel.class));
    }

    @Test
    public void testGetFromBackup() throws Exception {
        verifyGet(IModelHolder.GET_FROM_BACKUP);
    }

    @Test
    public void testGetFromDataStore() throws Exception {
        verifyGet(IModelHolder.GET_FROM_DATA_STORE);
    }

    private void verifyGet(boolean fromDataStore) {
        GetUrlRulesTask task = new GetUrlRulesTask(fromDataStore, urlRulesHolder, modelTranslationService, namespacedListsHolder);
        Result result = task.handle(new ModelContext());

        Assert.assertTrue(result.isSuccessful());
        Assert.assertNotNull(result.getContext().getUrlRules());
        Assert.assertNotNull(result.getContext().getUrlRulesModel());

        verify(urlRulesHolder, times(1)).load(eq(fromDataStore));
        verify(urlRulesHolder, never()).load(eq(!fromDataStore));
    }

    private static URLRules newValidModel() {
        URLRules model = new URLRules();

        model.setDefaultStatement(new Default());
        UrlRule defaultRule = new UrlRule();
        defaultRule.setIpProtocolVersion("4");
        defaultRule.setPort("1000");
        defaultRule.setProtocol("http");
        defaultRule.setUrn("test");
        model.getDefaultStatement().setUrlRule(defaultRule);

        return model;
    }
}
