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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.apps.e2e.tasks;

import com.comcast.apps.e2e.utils.StackRegistration;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackBackup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicStackRegistrationTask implements IProcessTask {
    private static final Logger log = LoggerFactory.getLogger(StaticStackRegistrationTask.class);

    @Override
    public Boolean handle(Context context) {
        String serviceName = context.getServiceName();
        StackBackup stackBackup = context.getStackBackup();
        if (stackBackup != null) {
            StackRegistration stackRegistration = new StackRegistration(stackBackup);
            stackRegistration.registerStacks();
            log.info("Stacks have been registered successfully for '{}'", serviceName);
        }
        return true;
    }
}
