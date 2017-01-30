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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.apps.e2e.managers;

import com.comcast.apps.e2e.helpers.FilesPathHelper;
import com.comcast.apps.e2e.tasks.Context;
import com.comcast.apps.e2e.utils.FileUtil;
import com.comcast.apps.e2e.utils.TestHelper;
import com.comcast.redirector.api.model.RedirectorConfig;
import com.comcast.redirector.common.serializers.JAXBContextBuilder;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.XMLSerializer;
import com.comcast.redirector.ruleengine.repository.NamespacedListsBatch;
import com.comcast.redirector.common.serializers.core.JsonSerializer;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

public class LoadCommonEntityManagerTest {
    private final static String SERVICE_NAME = "testApp";

    private static CommonEntityLoader loadCommonEntityManager;
    private static Context context;

    @BeforeClass
    public static void setUp() throws IOException {
        Serializer jsonSerializer = new JsonSerializer();
        Serializer xmlSerializer = new XMLSerializer(new JAXBContextBuilder().createContextForXML());
        FileUtil fileUtil = spy(new FileUtil(jsonSerializer, xmlSerializer));
        FilesPathHelper filesPathHelper = new FilesPathHelper(TestHelper.getResourcesPath());
        context = new Context(SERVICE_NAME, "baseUrl");
        loadCommonEntityManager = new CommonEntityLoader(fileUtil, filesPathHelper, context);

        String namespacedFileName = filesPathHelper.getFilename(FilesPathHelper.TestEntity.NAMESPACED_LISTS);
        String redirectorConfigFileName = filesPathHelper.getFilename(FilesPathHelper.TestEntity.REDIRECTOR_CONFIG);

        when(fileUtil.load(namespacedFileName)).thenReturn(TestHelper.load(namespacedFileName));
        when(fileUtil.load(redirectorConfigFileName)).thenReturn(TestHelper.load(redirectorConfigFileName));
    }

    @Test
    public void loadTest() {
        assertTrue(loadCommonEntityManager.load());
        assertEquals(getNamespacedListsBatch().getNamespacedLists(), context.getNamespaces().getNamespacedLists());
        assertEquals(getRedirectorConfig(), context.getRedirectorConfig());
    }

    private NamespacedListsBatch getNamespacedListsBatch() {
        Map<String, Set<String>> namespacedLists = new HashMap<>();
        namespacedLists.put("test_ip", Collections.singleton("192.168.1.1"));
        namespacedLists.put("testNS", Collections.singleton("valueTest"));
        NamespacedListsBatch namespacedListsBatch = new NamespacedListsBatch();
        namespacedListsBatch.setNamespacedLists(namespacedLists);
        return namespacedListsBatch;
    }

    private RedirectorConfig getRedirectorConfig() {
        RedirectorConfig redirectorConfig = new RedirectorConfig(1, 1);
        redirectorConfig.setVersion(1444140317424L);
        return redirectorConfig;
    }
}
