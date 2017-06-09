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

package com.comcast.redirector.dataaccess;

import org.apache.commons.lang3.StringUtils;

import static com.comcast.redirector.dataaccess.EntityCategory.*;

public enum EntityType {
    DISTRIBUTION_WITH_DEFAULT_AND_FALLBACK_SERVERS("distributionWithDefaultAndFallbackServers", REDIRECTOR),
    RULE("rules", REDIRECTOR),
    TEMPLATE_RULE("templateRules", REDIRECTOR),
    TEMPLATE_URL_RULE("templateUrlRules", REDIRECTOR),
    URL_RULE("urlRules", REDIRECTOR),
    URL_PARAMS("urlParams", REDIRECTOR),
    SERVER("server", REDIRECTOR),
    DISTRIBUTION("distribution", REDIRECTOR),
    WHITELIST("whitelisted", REDIRECTOR),
    WHITELIST_UPDATES("whitelistedUpdates", REDIRECTOR),
    STACK_COMMENTS("stackComments", REDIRECTOR),
    NAMESPACED_LIST("namespacedList", GLOBAL),
    PENDING_CHANGES_STATUS("pendingChangesStatus", REDIRECTOR),
    CORE_BACKUP_PENDING_CHANGES_STATUS("CoreBackupPendingChangesStatus", REDIRECTOR),
    CORE_BACKUP_NAMESPACES_CHANGES_STATUS("CoreBackupNamespacesChangesStatus", REDIRECTOR),
    TEST_CASE("testCases", REDIRECTOR),
    MODEL_CHANGED("modelChanged", REDIRECTOR),
    MODEL_REFRESH("modelRefresh", REDIRECTOR),
    STACKS_RELOAD("stacksReload", REDIRECTOR),
    STACKS_CHANGED("stacksChanged", REDIRECTOR),
    SERVICES_CHANGED("servicesChanged", DATA_VERSIONS),
    BACKUP("backup", REDIRECTOR),
    STACK("services", GLOBAL),
    PARTNERS("partners", DECIDER),
    DECIDER_RULE("rules", DECIDER),
    PENDING_DISTRIBUTIONS("PendindChangeDistributions", REDIRECTOR),
    PENDING_STACKS("pendingStacks", REDIRECTOR),
    INSTANCES(StringUtils.EMPTY, XRE_INSTANCES),
    APPLICATIONS("", REDIRECTOR),
    CONFIG("xreconfig", GLOBAL);

    private String path;
    private EntityCategory category;

    EntityType(String path, EntityCategory category) {
        this.path = path;
        this.category = category;
    }

    public String getPath() {
        return path;
    }

    public EntityCategory getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
