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

public class IntegrationTestEvent<T> {
    public enum Type {
        MODEL_INIT, MODEL_INIT_FAILS, MODEL_UPDATE, STACKS_UPDATE, STACKS_RELOAD, NAMESPACED_LIST_APPLIED
    }

    private Type type;
    private Class<T> dataClass;
    private T data;

    public Type getType() {
        return type;
    }

    public Class<T> getDataClass() {
        return dataClass;
    }

    public T getData() {
        return data;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setDataClass(Class<T> dataClass) {
        this.dataClass = dataClass;
    }

    public void setData(T data) {
        this.data = data;
    }
}
