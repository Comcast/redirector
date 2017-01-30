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
 * @author Stanislav Menshykov (smenshykov@productengine.com)
 */

package com.comcast.apps.e2e.utils;

import com.comcast.apps.e2e.config.E2EConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonUtils {
    private static Logger log = LoggerFactory.getLogger(CommonUtils.class);

    public static void waitForHostCachesToUpdate() {
        try {
            Thread.sleep(E2EConfigLoader.getDefaultInstance().getDiscoveryPullInterval());
        } catch (InterruptedException e) {
            log.warn("Interrupted while waiting for caches to update: " + e);
        }
    }
}
