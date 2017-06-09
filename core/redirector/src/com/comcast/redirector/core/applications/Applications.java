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

package com.comcast.redirector.core.applications;

import com.google.common.collect.Sets;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashSet;
import java.util.Set;
@XmlRootElement(name = "applications")
public class Applications {
    private Set<String> apps = new LinkedHashSet<>();

    public Applications() {
    }

    public Applications(Set<String> apps) {
        this.apps = apps;
    }

    public Set<String> getApps() {
        return apps;
    }

    public void addApp(String app) {
        apps.add(app);
    }

    public Applications diff(Applications from) {
        return new Applications(new LinkedHashSet<>(Sets.difference(this.apps, from.apps)));
    }
}
