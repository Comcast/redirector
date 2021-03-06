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

package com.comcast.redirector.core.engine;

import com.comcast.redirector.common.InstanceInfo;
import com.comcast.redirector.ruleengine.model.ServerGroup;

import java.io.Closeable;
import java.util.Map;

public interface IRedirector extends Closeable {
    InstanceInfo redirect(Map<String, String> context);
    ServerGroup redirectServerGroup(ServerGroup serverGroup, Map<String, String> context);
    void suspendPolling();
    void restartPollingIfSuspended();
}
