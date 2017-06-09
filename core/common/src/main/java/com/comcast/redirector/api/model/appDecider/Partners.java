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
package com.comcast.redirector.api.model.appDecider;

import com.comcast.redirector.api.model.Expressions;

import javax.xml.bind.annotation.*;
import java.util.LinkedHashSet;
import java.util.Set;

@XmlRootElement(name = "partners")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({Partner.class, PartnerProperty.class})
public class Partners implements Expressions {

    @XmlAnyElement(lax = true)
    private Set<Partner> partners;

    public Set<Partner> getPartners() {
        return partners;
    }

    public void setPartners(Set<Partner> partners) {
        this.partners = partners;
    }

    public void addPartner(Partner partner) {
        if (partners == null) {
            partners = new LinkedHashSet<>();
        }
        partners.add(partner);
    }
}
