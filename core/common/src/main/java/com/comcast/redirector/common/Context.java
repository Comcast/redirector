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

package com.comcast.redirector.common;

import com.comcast.redirector.api.model.Value;

import java.util.*;

public class Context {
    public static final String SESSION_ID = "rSessionId";

    private Map<String, String> context;

    private Context(Builder builder) {
        this.context = builder.context;
    }

    public Map<String, String> asMap() {
        return Collections.unmodifiableMap(context);
    }

    public static class Builder {
        private Map<String, String> context = new HashMap<>();

        public Builder addMap(Map source) {
            if (source != null && source.size() > 0) {
                Set<Map.Entry<String, Object>> entrySet = source.entrySet();
                for (Map.Entry<String, Object> entry : entrySet) {
                    String valueToPut = (entry.getValue() == null ? "null" : entry.getValue().toString());
                    context.put(entry.getKey(), valueToPut);
                }
            }

            return this;
        }

        public Builder addEntry(String key, List<Value> values) {
            if (values.size() > 0) {
                String value = (values.size() > 1) ? values.toString() : values.get(0).getValue();
                context.put(key, value);
            }

            return this;
        }

        public Builder addEntry(String key, String value) {
            context.put(key, value);

            return this;
        }

        public String get(String key) {
            return context.get(key);
        }

        public Context build() {
            return new Context(this);
        }
    }
}
