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

package com.comcast.redirector.core.spring;

import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class AppScope implements Scope {
    private Map<String, AppBeansHolder> scopes = Collections.synchronizedMap(new HashMap<>());

    public static final String APP_SCOPE = "app";

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        return getScope().getBean(name, beanName -> objectFactory.getObject());
    }

    @Override
    public Object remove(String name) {
        return getScope().removeBean(name);
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {

    }

    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }

    @Override
    public String getConversationId() {
        return AppsContextHolder.getCurrentApp();
    }

    @VisibleForTesting
    public Set<String> getApps() {
        return scopes.keySet();
    }

    private AppBeansHolder getScope() {
        return scopes.computeIfAbsent(getConversationId(), key -> new AppBeansHolder());
    }

    private class AppBeansHolder {
        private Map<String, Object> beans = new HashMap<>();

        public synchronized Object getBean(String key, Function<String, Object> factoryMethod) {
            Object value = beans.get(key);
            if (value == null) {
                value = factoryMethod.apply(key);
                beans.put(key, value);
            }
            return value;
        }

        public synchronized Object removeBean(String key) {
            return beans.remove(key);
        }
    }
}
