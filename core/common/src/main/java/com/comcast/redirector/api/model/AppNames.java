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
 * @author Alexey Smolenskiy (asmolenskiy@productengine.com)
 */

package com.comcast.redirector.api.model;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@XmlRootElement(name = "appNamesList")
@XmlAccessorType(XmlAccessType.FIELD)
public class AppNames implements Serializable{

    @XmlAttribute
    long version = 0;

    @XmlElement(name = "appNames")
    Set<String> appNames = new HashSet<>();

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public AppNames() {}

    public Set<String> getAppNames() {
        return appNames;
    }

    public void setAppNames(Set<String> appNames) {
        this.appNames = appNames;
    }

    public void add(String s) {
        appNames.add(s);
    }

    public boolean remove(String s) {
        return appNames.remove(s);
    }
}
