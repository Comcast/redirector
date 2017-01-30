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

package com.comcast.redirector;

import com.comcast.redirector.core.IRedirectorFactory;
import com.comcast.redirector.core.applications.IApplicationsManager;
import com.comcast.redirector.core.engine.IRedirector;
import com.comcast.redirector.core.spring.RedirectorGatewayBeans;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Set;

class RedirectorGatewayRootContext implements RootContext {

    public static RootContext getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static RootContext INSTANCE = new RedirectorGatewayRootContext();
    }

    private RedirectorGatewayRootContext() {
    }

    private ApplicationContext context;

    @Override
    public void start() {
        context = new AnnotationConfigApplicationContext(RedirectorGatewayBeans.class);
        new Thread(() -> getApplicationsManager().start(), "StartApplicationManager").start();
    }

    @Override
    public IRedirector getRedirector(String appName) {
        return context != null ? context.getBean(IRedirectorFactory.class).createRedirector(appName) : null;
    }

    @Override
    public boolean isAppRegistered(String appName) {
        return getApplicationsManager().getApplications().getApps().contains(appName);
    }

    @Override
    public Set<String> getApplications() {
        return getApplicationsManager().getApplications().getApps();
    }

    private IApplicationsManager getApplicationsManager() {
        return context.getBean(IApplicationsManager.class);
    }
}
