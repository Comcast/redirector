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

import java.util.function.Consumer;

public class IntegrationTestChangeListenerImpl<T> implements IntegrationTestChangeListener<T>  {
    private Consumer<IntegrationTestEvent<T>> command;
    private Class<T> eventClass;
    private IntegrationTestEvent.Type defaultEventType;

    public IntegrationTestChangeListenerImpl(Class<T> eventClass, IntegrationTestEvent.Type defaultEventType) {
        this.eventClass = eventClass;
        this.defaultEventType = defaultEventType;
    }

    @Override
    public void setCallback(Consumer<IntegrationTestEvent<T>> callback) {
        this.command = callback;
    }

    @Override
    public void update(T data) {
        if (command != null) command.accept(createEvent(data));
    }

    private IntegrationTestEvent<T> createEvent(T data) {
        IntegrationTestEvent<T> event = new IntegrationTestEvent<>();
        event.setData(data);
        event.setDataClass(eventClass);
        event.setType(defaultEventType);

        return event;
    }
}
