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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package com.comcast.redirector.api.model.whitelisted;

import com.comcast.redirector.api.model.ActionType;
import com.comcast.redirector.api.model.Expressions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Objects;

@XmlRootElement(name = "paths")
@XmlAccessorType(XmlAccessType.FIELD)
public class WhitelistUpdate implements Serializable, Expressions {
    @XmlElement(name = "path")
    String path; //po/poc6
    @XmlElement(name = "updated")
    long updated; //timeunit
    @XmlElement(name = "action")
    ActionType action; //ADD,DELETE

    public WhitelistUpdate() {
    }

    public WhitelistUpdate(String path) {
        this.path = path;
    }


    public WhitelistUpdate(String path, long updated, ActionType action) {
        this.path = path;
        this.updated = updated;
        this.action = action;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public ActionType getAction() {
        return action;
    }

    public void setAction(ActionType action) {
        this.action = action;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WhitelistUpdate)) return false;
        WhitelistUpdate that = (WhitelistUpdate) o;
        return Objects.equals(getPath(), that.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPath(), getUpdated(), getAction());
    }
}


