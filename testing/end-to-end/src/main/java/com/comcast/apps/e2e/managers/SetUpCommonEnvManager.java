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

import com.comcast.apps.e2e.helpers.ServiceHelper;
import com.comcast.apps.e2e.helpers.ServicePathHelper;
import com.comcast.apps.e2e.tasks.Context;
import com.comcast.redirector.api.model.RedirectorConfig;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.namespaced.NamespacedListValueForWS;
import com.comcast.redirector.ruleengine.repository.NamespacedListsBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SetUpCommonEnvManager implements ISetUpEnvManager {
    private static final Logger log = LoggerFactory.getLogger(SetUpEnvManager.class);

    private final ServicePathHelper servicePathHelper;
    private final Context context;
    private final ServiceHelper serviceHelper;

    public SetUpCommonEnvManager(Context context, ServiceHelper serviceHelper) {
        this.servicePathHelper = context.getServicePathHelper();
        this.context = context;
        this.serviceHelper = serviceHelper;
    }

    public void setUp() {
        RedirectorConfig redirectorConfig = context.getRedirectorConfig();
        NamespacedListsBatch namespaces = context.getNamespaces();
        setUpRedirectorConfig(redirectorConfig);
        setUpNamespacedLists(namespaces);
    }

    private void setUpRedirectorConfig(RedirectorConfig redirectorConfig) {
        try {
            String redirectorConfigServicePath = servicePathHelper.getRedirectorConfigServicePath();
            serviceHelper.post(redirectorConfigServicePath, redirectorConfig, MediaType.APPLICATION_JSON);
            log.info("Successfully saved redirectorConfig application.");
        } catch (WebApplicationException wae) {
            String error = String.format("Failed to save redirectorConfig.");
            throw new WebApplicationException(error, wae.getResponse().getStatus());
        }
    }

    private void setUpNamespacedLists(NamespacedListsBatch namespaces) {
        for (Map.Entry<String, Set<String>> cursor : namespaces.getNamespacedLists().entrySet()) {
            String namespacedListName = cursor.getKey();
            Set<String> values = cursor.getValue();
            try {
                NamespacedList namespacedList = createNamespacedList(namespacedListName, values);
                String namespacedListServicePath = servicePathHelper.getNamespacedListPostOneNamespacedServicePath(namespacedListName) + "";
                serviceHelper.post(namespacedListServicePath, namespacedList, MediaType.APPLICATION_JSON);
                log.info("Successfully saved namespacedList. Name = '{}'", namespacedListName);
            } catch (WebApplicationException wae) {
                String error = String.format("Failed to save namespacedList. Name = '%s'", namespacedListName);
                throw new WebApplicationException(error, wae.getResponse().getStatus());
            }
        }
    }

    private NamespacedList createNamespacedList(String namespacedListName, Set<String> values) {
        NamespacedList namespacedList = new NamespacedList();
        namespacedList.setName(namespacedListName);
        Set<NamespacedListValueForWS> valuesSet = new HashSet<>();
        for (String value : values) {
            NamespacedListValueForWS value1 = new NamespacedListValueForWS(value);
            valuesSet.add(value1);
        }
        namespacedList.setValueSet(valuesSet);
        return namespacedList;
    }
}
