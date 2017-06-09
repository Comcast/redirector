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

package com.comcast.redirector.api.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@XmlRootElement(name="redirectorConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class RedirectorConfig {

    @XmlAttribute
    private long version = 0;

    private int minHosts;
    private int appMinHosts;

    public RedirectorConfig() {
    }

    public RedirectorConfig(int minHosts, int appMinHosts) {
        this.minHosts = minHosts;
        this.appMinHosts = appMinHosts;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public int getMinHosts() {
        return minHosts;
    }

    public void setMinHosts(int minHosts) {
        this.minHosts = minHosts;
    }

    public int getAppMinHosts() {
        return appMinHosts;
    }

    public void setAppMinHosts(int appMinHosts) {
        this.appMinHosts = appMinHosts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RedirectorConfig)) return false;
        RedirectorConfig that = (RedirectorConfig) o;
        return Objects.equals(getMinHosts(), that.getMinHosts()) &&
                Objects.equals(getAppMinHosts(), that.getAppMinHosts());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMinHosts(), getAppMinHosts() );
    }
}
