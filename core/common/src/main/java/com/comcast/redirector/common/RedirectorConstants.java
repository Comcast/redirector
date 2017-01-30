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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package com.comcast.redirector.common;

public class RedirectorConstants {

    //todo: typo (DELIMITER)
    public static final String DELIMETER = "/";
    public static final String AUTH_CONTROLLER_PATH = DELIMETER + "auth";
    public static final String BACKUP_CONTROLLER_PATH = DELIMETER + "backups";
    public static final String DISTRIBUTION_CONTROLLER_PATH = DELIMETER + "distributions";
    public static final String DISTRIBUTION_OFFLINE_CONTROLLER_PATH = DELIMETER + "distributionsOffline";
    public static final String NAMESPACED_LISTS = "namespacedLists";
    public static final String NAMESPACE_CONTROLLER_PATH = DELIMETER + NAMESPACED_LISTS;
    public static final String NAMESPACE_OFFLINE_CONTROLLER_PATH = DELIMETER + "namespacedListsOffline";
    public static final String STACKS_CONTROLLER_PATH = DELIMETER +"stacks";
    public static final String STACKS_CONTROLLER_PATH_DELETE_ALL_INACTIVE = STACKS_CONTROLLER_PATH + DELIMETER + "deleteStacks";
    public static final String PATHS_OFFLINE_CONTROLLER_PATH = DELIMETER +"stacksOffline";
    public static final String REDIRECTOR_OFFLINE_CONTROLLER_PATH = DELIMETER + "redirectorOffline";
    public static final String SERVERS_CONTROLLER_PATH = DELIMETER +"servers";
    public static final String SERVERS_OFFLINE_CONTROLLER_PATH = DELIMETER +"serversOffline";
    public static final String DISTRIBUTION_WITH_DEFAULT_CONTROLLER_PATH = DELIMETER +"export";
    public static final String DISTRIBUTION_WITH_DEFAULT_CONTROLLER_PATH_OFFLINE = DELIMETER +"exportOffline";
    public static final String WHITELISTED_CONTROLLER_PATH = DELIMETER +"whitelist";
    public static final String STACK_COMMENTS_CONTROLLER_PATH = DELIMETER +"stackComments";
    public static final String WHITELISTED_UPDATES_CONTROLLER_PATH = DELIMETER +"whitelistWithTimestamps";
    public static final String WHITELISTED_OFFLINE_CONTROLLER_PATH = DELIMETER +"whitelistOffline";
    public static final String PENDING_CONTROLLER_PATH = DELIMETER + "changes";
    public static final String CORE_BACKUP_PENDING_CONTROLLER_PATH = DELIMETER + "coreBackupChanges";
    public static final String PENDING_OFFLINE_CONTROLLER_PATH = DELIMETER + "changesOffline";
    public static final String RULES_CONTROLLER_PATH = DELIMETER + "rules";
    public static final String RULES_OFFLINE_CONTROLLER_PATH = DELIMETER + "rulesOffline";
    public static final String URL_RULES_CONTROLLER_PATH = DELIMETER + "urlRules";
    public static final String URL_RULES_OFFLINE_CONTROLLER_PATH = DELIMETER + "urlRulesOffline";
    public static final String REDIRECTOR_PATH = DELIMETER;
    public static final String REDIRECTOR_TEST_SUITE_PATH = DELIMETER + "testSuite";
    public static final String REDIRECTOR_ZOOKEEPER_PATH = "Redirector";
    public static final String REDIRECTOR_LOCK_PATH = "locks";
    public static final String SERVICES_PATH = "services";
    public static final String FULL_SERVICES_PATH = DELIMETER + SERVICES_PATH;
    public static final String SERVICE_NAME_GUIDE_PATH = "xreGuide";
    public static final String SERVICE_NAME_APP_PATH = "xreApp";
    public static final String RULES_PATH = "rules";
    public static final String SERVER_PATH = "server";
    public static final String BACKUP_ROOT_PATH = "backup";
    public static final String BACKUP_TRIGGER_PATH = "trigger";
    public static final String BACKUP_USAGE_SCHEDULE_PATH = "usageSchedule";
    public static final String DISTRIBUTION_PATH = "distribution";
    public static final String MODEL_RELOAD_PATH = "modelReload";
    public static final String STACKS_RELOAD_PATH = "stacksReload";
    public static final String WHITELISTED_PATH = "whitelisted";
    public static final String PENDING = "pending";
    public static final String SUMMARY_PATH = DELIMETER + "summary";
    public static final String SETTINGS = DELIMETER + "settings";
    public static final String TRAFFIC_PATH = DELIMETER + "traffic";
    public static final String WEIGHT_CALCULATOR_PATH = DELIMETER + "weightCalculator";
    public static final String END_TO_END_PATH = DELIMETER + "endToEnd";
    public static final String END_TO_END_REPORT_PATH = DELIMETER + "testReport";
    public static final String REDIRECTOR_CONTROLLER_PATH = DELIMETER + "redirectorService";
    public static final String INITIALIZER_CONTROLLER_PATH = DELIMETER + "redirectorInitModel";


    public static final String TEMPLATES_RULE_CONTROLLER_PATH = DELIMETER + "templates" + RULES_CONTROLLER_PATH;
    public static final String TEMPLATES_RULE_OFFLINE_CONTROLLER_PATH = DELIMETER + "templates" + RULES_OFFLINE_CONTROLLER_PATH;
    public static final String TEMPLATES_URL_RULE_CONTROLLER_PATH = DELIMETER + "templates" + URL_RULES_CONTROLLER_PATH;
    public static final String TEMPLATES_URL_RULE_OFFLINE_CONTROLLER_PATH = DELIMETER + "templates" + URL_RULES_OFFLINE_CONTROLLER_PATH;

    public static final String DEFAULT_SERVER_NAME = "default";

    public static final String VALUE_TYPE_NONE = "none";
    public static final String HOST_PLACEHOLDER = "{host}";
    public static final String PORT_PLACEHOLDER = "{port}";
    public static final String PROTOCOL_PLACEHOLDER = "{protocol}";
    public static final String URN_PLACEHOLDER = "{urn}";

    public static final String URL_TEMPLATE = PROTOCOL_PLACEHOLDER + "://" + HOST_PLACEHOLDER + ":" + PORT_PLACEHOLDER + "/" + URN_PLACEHOLDER;

    public static final String DEFAULT_URL_RULE = "default";
    public static final int NO_MODEL_NODE_VERSION = -1;

    public static final String PENDING_STATUS_APPROVE = "approve";
    public static final String PENDING_STATUS_PATH_RULES = "pathRules";
    public static final String PENDING_STATUS_URL_RULES = "urlRules";
    public static final String PENDING_STATUS_URL_PARAMS = "urlParams";
    public static final String PENDING_STATUS_SERVERS = "servers";
    public static final String PENDING_STATUS_DISTRIBUTIONS = "distributions";
    public static final String PENDING_STATUS_STACKMANAGEMENT= "stackmanagement";
    public static final String PENDING_STATUS_TEMPLATE_PATH_RULES = "templatePathRules";
    public static final String PENDING_STATUS_TEMPLATE_URL_PATH_RULES = "templateUrlPathRules";

    public static final String BACKUPFILE_SELECT_SERVER = "selectserver.xml";
    public static final String BACKUPFILE_URL_RULES = "urlrules.xml";
    public static final String BACKUPFILE_WHITELIST = "whitelist.xml";
    public static final String BACKUPFILE_WHITELIST_UPDATES = "whitelist_updates.xml";
    public static final String BACKUPFILE_URL_TEMPLATES = "urlTemplates.json";
    public static final String BACKUPFILE_FLAVOR_TEMPLATES = "flavorTemplates.json";
    public static final String BACKUPFILE_MODELMETADATA = "modelmetadata.json";


    public static final String USER_AGENT_STRING = "redirectorGateway";

    public static class Parameters {
        public static final String ACCOUNT_ID = "serviceAccountId";
    }

    // Authentication
    public static class AuthConstants {
        public static final String PROD_PROFILE = "prod";
        public static final String DEV_USER = "dev";
    }

    public static class Logging {
        public static final String APP_NAME_PREFIX = "rapp=";
        public static final String ENTITY_NAME_PREFIX = "rentity=";
        public static final String EXECUTION_STEP_PREFIX = "executionStep=";
        public static final String EXECUTION_FLOW_PREFIX = "executionFlow=";
        public static final String OPERATION_RESULT = "opResult=";
    }
    
    public static class EndpointPath {
        public static final String APPLICATION_NAMES = "applicationNames";
        public static final String GET_ALL_REGISTERED_APPS = "getAllRegisteredApps";
        public static final String VALID_MODEL_EXISTS = "validModelExists";
    }
}
