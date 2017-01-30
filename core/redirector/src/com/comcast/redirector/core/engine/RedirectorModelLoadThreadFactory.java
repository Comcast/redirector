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

package com.comcast.redirector.core.engine;

import com.comcast.redirector.common.RedirectorConstants;

import java.util.concurrent.ThreadFactory;

public class RedirectorModelLoadThreadFactory implements ThreadFactory {
    private final ThreadGroup group;
    private final String name;

    RedirectorModelLoadThreadFactory(String appName) {
        group = new ThreadGroup("RedirectorModelGroup");
        name = getModelUpdateThreadName(appName);
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(group, r, name);
    }

    public static String getModelUpdateThreadName(String appName) {
        return RedirectorConstants.Logging.APP_NAME_PREFIX + appName;
    }
}
