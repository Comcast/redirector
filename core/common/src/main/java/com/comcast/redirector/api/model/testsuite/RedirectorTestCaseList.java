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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.api.model.testsuite;

import javax.xml.bind.annotation.*;
import java.util.Collection;

@XmlRootElement(name = "testCases")
@XmlSeeAlso(RedirectorTestCase.class)
@XmlAccessorType(XmlAccessType.FIELD)
public class RedirectorTestCaseList {

    @XmlElementWrapper(name = "items")
    @XmlElements(@XmlElement(name = "testCase", type = RedirectorTestCase.class))
    private Collection<RedirectorTestCase> items;

    public Collection<RedirectorTestCase> getRedirectorTestCases() {
        return items;
    }

    public void setRedirectorTestCases(Collection<RedirectorTestCase> redirectorTestCases) {
        this.items = redirectorTestCases;
    }

    public int size() {
        return items == null ? 0 : items.size();
    }
}
