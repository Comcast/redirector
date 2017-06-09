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

package com.comcast.redirector.api.redirector.service;

import com.comcast.redirector.api.ApplicationStatusMode;
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;

public interface IWhiteListService {

    Whitelisted getWhitelistedStacks(String serviceName);

    Whitelisted addWhitelistedStacks(String serviceName, Whitelisted whitelisted);

    void deleteWhitelistedStacks(String serviceName, String values);

    void saveWhitelistedStacks(Whitelisted whitelisted, String serviceName);

    OperationResult saveWhitelistedStacks(String serviceName, Whitelisted pending, ApplicationStatusMode mode);
}
