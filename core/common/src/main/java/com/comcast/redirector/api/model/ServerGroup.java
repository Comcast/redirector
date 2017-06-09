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
 */
package com.comcast.redirector.api.model;

/* ===========================================================================
 * Copyright (c) 2014 Comcast Corp. All rights reserved.
 * ===========================================================================
 *
 * Author: Paul Guslisty
 * Created: 2/6/14 3:56 PM
 */

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@XmlRootElement(name = "serverGroup")
@XmlSeeAlso({Server.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class ServerGroup extends VisitableExpression implements Serializable, Expressions {

    @XmlAttribute(name="enablePrivate")
    private String enablePrivate;

    @XmlAttribute(name="countDownTime")
    private String countDownTime;

    @XmlElement(name = "server")
    private List<Server> servers;

    public List<Server> getServers() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    public String getEnablePrivate() {
        return enablePrivate;
    }

    public void setEnablePrivate(String enablePrivate) {
        this.enablePrivate = enablePrivate;
    }

    public String getCountDownTime() {
        return countDownTime;
    }

    public void setCountDownTime(String countDownTime) {
        this.countDownTime = countDownTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerGroup that = (ServerGroup) o;
        return Objects.equals(enablePrivate, that.enablePrivate) &&
                Objects.equals(countDownTime, that.countDownTime) &&
                Objects.equals(servers, that.servers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enablePrivate, countDownTime, servers);
    }
}
