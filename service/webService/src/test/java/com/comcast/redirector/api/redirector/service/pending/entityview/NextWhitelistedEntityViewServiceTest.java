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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package com.comcast.redirector.api.redirector.service.pending.entityview;

import com.comcast.redirector.api.model.ActionType;
import com.comcast.redirector.api.model.Value;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.redirector.service.IWhiteListService;
import com.comcast.redirector.api.redirector.service.pending.IChangesStatusService;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NextWhitelistedEntityViewServiceTest {
    private IWhiteListService whiteListService;
    private IChangesStatusService changesStatusService;
    private NextWhitelistedEntityViewService testee;

    @Before
    public void setUp() throws Exception {
        whiteListService = mock(IWhiteListService.class);
        changesStatusService = mock(IChangesStatusService.class);
        testee = new NextWhitelistedEntityViewService();
        testee.setChangesStatusService(changesStatusService);
        testee.setWhiteListService(whiteListService);
    }

    @Test
    public void testGetEntity() throws Exception {
        String serviceName = "xreGuide";
        Whitelisted currentWhitelist = new Whitelisted();
        currentWhitelist.setPaths(new ArrayList<String>() {{
            add("/P0/1.55");
            add("/P1/3.44");
        }});

        when(whiteListService.getWhitelistedStacks(serviceName)).thenReturn(currentWhitelist);

        PendingChangesStatus pendingChangesStatus = new PendingChangesStatus();
        Map<String, PendingChange> whitelistedChanges = new HashMap<>();
        PendingChangesHelper.putPendingChange(whitelistedChanges, Integer.toString("/P0/3.355".hashCode()), new Value("/P0/3.355"), null, ActionType.ADD);
        PendingChangesHelper.putPendingChange(whitelistedChanges, Integer.toString("/P2/3.22".hashCode()), new Value("/P2/3.22"), null, ActionType.ADD);
        PendingChangesHelper.putPendingChange(whitelistedChanges, Integer.toString("/P1/3.44".hashCode()), null, new Value("/P1/3.44"), ActionType.DELETE);
        pendingChangesStatus.setWhitelisted(whitelistedChanges);
        when(changesStatusService.getPendingChangesStatus(serviceName)).thenReturn(pendingChangesStatus);

        Whitelisted whitelisted = testee.getEntity(serviceName);
        Assert.assertEquals("/P0/1.55", whitelisted.getPaths().get(0));
        Assert.assertEquals("/P2/3.22", whitelisted.getPaths().get(1));
        Assert.assertEquals("/P0/3.355", whitelisted.getPaths().get(2));
    }
}
