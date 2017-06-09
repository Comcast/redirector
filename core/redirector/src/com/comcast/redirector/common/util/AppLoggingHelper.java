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

package com.comcast.redirector.common.util;

import com.comcast.redirector.common.RedirectorConstants;

public class AppLoggingHelper {
    private ThreadLocalLogger log;
    private String appName;
    private int modelVersion;

    public AppLoggingHelper(ThreadLocalLogger log, String appName, int modelVersion) {
        this.log = log;
        this.appName = appName;
        this.modelVersion = modelVersion;
    }

    private String getMessageTemplateForApp(String message) {
        return RedirectorConstants.Logging.APP_NAME_PREFIX + appName + " rmv=" + modelVersion
            + " app=" + appName + ".version=" + modelVersion + " : " + message;
    }

    public void info(String message, Object... arguments) {
        log.info(getMessageTemplateForApp(message), arguments);
    }

    public void error(String message, Object... arguments) {
        log.error(getMessageTemplateForApp(message), arguments);
    }

    public void warn(String message, Object... arguments) {
        log.warn(getMessageTemplateForApp(message), arguments);
    }

    public void debug(String message, Object... arguments) {
        log.debug(getMessageTemplateForApp(message), arguments);
    }
}
