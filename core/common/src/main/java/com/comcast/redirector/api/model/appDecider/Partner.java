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
 */
package com.comcast.redirector.api.model.appDecider;

import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.VisitableExpression;

import javax.xml.bind.annotation.*;
import java.util.Set;

@XmlRootElement(name = "partner")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso(PartnerProperty.class)
public class Partner extends VisitableExpression implements Expressions {
    public Partner() {
    }

    public Partner(String id) {
        this.id = id;
    }

    @XmlElementWrapper(name="properties")
    @XmlElements(
            @XmlElement(name="property", type=PartnerProperty.class)
    )
    private Set<PartnerProperty> properties;

    @XmlAttribute(name = "id")
    private String id;

    public Set<PartnerProperty> getProperties() {
        return properties;
    }

    public void setProperties(Set<PartnerProperty> properties) {
        this.properties = properties;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
