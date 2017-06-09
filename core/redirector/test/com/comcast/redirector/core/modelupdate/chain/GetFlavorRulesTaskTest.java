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

import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.core.modelupdate.converter.ModelTranslationService;
import com.comcast.redirector.core.modelupdate.holder.IModelHolder;
import com.comcast.redirector.ruleengine.model.Model;
import com.comcast.redirector.ruleengine.repository.NamespacedListRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.*;

public class GetFlavorRulesTaskTest {
    private IModelHolder<SelectServer> flavorRulesHolder;
    private NamespacedListRepository namespacedListsHolder;
    private ModelTranslationService modelTranslationService;

    @Before
    public void setUp() throws Exception {
        flavorRulesHolder = (IModelHolder<SelectServer>)mock(IModelHolder.class);
        when(flavorRulesHolder.load(anyBoolean())).thenReturn(newValidModel());

        namespacedListsHolder = mock(NamespacedListRepository.class);

        modelTranslationService = mock(ModelTranslationService.class);
        when(modelTranslationService.translateFlavorRules(any(SelectServer.class), any(NamespacedListRepository.class)))
            .thenReturn(mock(Model.class));
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
        GetFlavorRulesTask task = new GetFlavorRulesTask(fromDataStore, flavorRulesHolder, modelTranslationService, namespacedListsHolder);

        Result result = task.handle(new ModelContext());

        Assert.assertTrue(result.isSuccessful());
        Assert.assertNotNull(result.getContext().getFlavorRules());
        Assert.assertNotNull(result.getContext().getFlavorRulesModel());

        verify(flavorRulesHolder, times(1)).load(eq(fromDataStore));
        verify(flavorRulesHolder, never()).load(eq(!fromDataStore));
    }

    private static SelectServer newValidModel() {
        SelectServer selectServer = new SelectServer();

        selectServer.setDistribution(new Distribution());
        selectServer.getDistribution().setDefaultServer(new Server());

        return selectServer;
    }
}
