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

import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.core.balancer.serviceprovider.whitelist.WhiteList;
import com.comcast.redirector.core.modelupdate.converter.ModelTranslationService;
import com.comcast.redirector.core.modelupdate.holder.IModelHolder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.*;

public class GetWhitelistedStacksTaskTest {
    private IModelHolder<Whitelisted> whiteListHolder;
    private ModelTranslationService modelTranslationService;

    @Before
    public void setUp() throws Exception {
        whiteListHolder = (IModelHolder<Whitelisted>)mock(IModelHolder.class);
        when(whiteListHolder.load(anyBoolean())).thenReturn(mock(Whitelisted.class));

        modelTranslationService = mock(ModelTranslationService.class);
        when(modelTranslationService.translateWhitelistedStacks(any(Whitelisted.class)))
            .thenReturn(mock(WhiteList.class));
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
        GetWhitelistedStacksTask task = new GetWhitelistedStacksTask(fromDataStore, whiteListHolder, modelTranslationService);
        Result result = task.handle(new ModelContext());

        Assert.assertTrue(result.isSuccessful());
        Assert.assertNotNull(result.getContext().getWhitelistedStacks());
        Assert.assertNotNull(result.getContext().getWhiteListModel());

        verify(whiteListHolder, times(1)).load(eq(fromDataStore));
        verify(whiteListHolder, never()).load(eq(!fromDataStore));
    }
}
