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

package com.comcast.redirector.api.model.search;

import javax.xml.bind.annotation.*;
import java.util.Collection;

@XmlRootElement(name = "namespacedList")
@XmlSeeAlso({RuleEntity.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class NamespacedListEntity {
    private String name;
    private String description;

    @XmlElement(type = RuleEntity.class)
    private Collection<RuleEntity> dependingFlavorRules;

    @XmlElement(type = RuleEntity.class)
    private Collection<RuleEntity> dependingTemplateFlavorRules;

    @XmlElement(type = RuleEntity.class)
    private Collection<RuleEntity> dependingUrlRules;

    @XmlElement(type = RuleEntity.class)
    private Collection<RuleEntity> dependingTemplateUrlRules;

    @XmlElement(type = RuleEntity.class)
    private Collection<RuleEntity> dependingDeciderRules;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Collection<RuleEntity> getDependingFlavorRules() {
        return dependingFlavorRules;
    }

    public void setDependingFlavorRules(Collection<RuleEntity> dependingFlavorRules) {
        this.dependingFlavorRules = dependingFlavorRules;
    }

    public Collection<RuleEntity> getDependingTemplateFlavorRules() {
        return dependingTemplateFlavorRules;
    }

    public void setDependingTemplateFlavorRules(Collection<RuleEntity> dependingTemplateFlavorRules) {
        this.dependingTemplateFlavorRules = dependingTemplateFlavorRules;
    }

    public Collection<RuleEntity> getDependingUrlRules() {
        return dependingUrlRules;
    }

    public void setDependingUrlRules(Collection<RuleEntity> dependingUrlRules) {
        this.dependingUrlRules = dependingUrlRules;
    }

    public Collection<RuleEntity> getDependingTemplateUrlRules() {
        return dependingTemplateUrlRules;
    }

    public void setDependingTemplateUrlRules(Collection<RuleEntity> dependingTemplateUrlRules) {
        this.dependingTemplateUrlRules = dependingTemplateUrlRules;
    }

    public Collection<RuleEntity> getDependingDeciderRules() {
        return dependingDeciderRules;
    }

    public void setDependingDeciderRules(Collection<RuleEntity> dependingDeciderRules) {
        this.dependingDeciderRules = dependingDeciderRules;
    }
}
