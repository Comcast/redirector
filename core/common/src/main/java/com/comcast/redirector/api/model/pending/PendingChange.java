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

package com.comcast.redirector.api.model.pending;

import com.comcast.redirector.api.model.ActionType;
import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.Value;
import com.comcast.redirector.api.model.distribution.Rule;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.Objects;

@XmlRootElement(name = "pendingChange")
@XmlSeeAlso({IfExpression.class, Server.class, Rule.class, ActionType.class, Value.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class PendingChange {
    private String id;
    private ActionType changeType;
    private Object changedExpression;
    private Object currentExpression;

    public PendingChange() {
    }

    public PendingChange(String id, ActionType changeType) {
        this.id = id;
        this.changeType = changeType;
    }

    public PendingChange(String id, ActionType changeType, Expressions changedExpression, Expressions currentExpression) {
        this.id = id;
        this.changeType = changeType;
        this.changedExpression = changedExpression;
        this.currentExpression = currentExpression;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ActionType getChangeType() {
        return changeType;
    }

    public void setChangeType(ActionType changeType) {
        this.changeType = changeType;
    }

    public Object getChangedExpression() {
        return changedExpression;
    }

    public void setChangedExpression(Expressions changedExpression) {
        this.changedExpression = changedExpression;
    }

    public Object getCurrentExpression() {
        return currentExpression;
    }

    public void setCurrentExpression(Expressions currentExpression) {
        this.currentExpression = currentExpression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PendingChange that = (PendingChange) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(changeType, that.changeType) &&
                Objects.equals(changedExpression, that.changedExpression) &&
                Objects.equals(currentExpression, that.currentExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, changeType, changedExpression, currentExpression);
    }
}
