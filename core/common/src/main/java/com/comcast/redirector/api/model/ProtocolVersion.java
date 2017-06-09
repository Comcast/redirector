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
 * @author Alexey Smolenskiy (asmolenskiy@productengine.com)
 */

package com.comcast.redirector.api.model;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

public enum ProtocolVersion {
    xre,
    xres,
    ws,
    wss,
    http,
    https,
    xmpp;

    private  static final Set<String> protocols = new HashSet<>(values().length);

    public static Set<String> getProtocols() {
        Set<String> unmodifiableSet = Collections.unmodifiableSet(protocols);
        return unmodifiableSet;
    }
    static {
        for (ProtocolVersion protocol : values()) {
            protocols.add(protocol.name());
        }
    }
}
