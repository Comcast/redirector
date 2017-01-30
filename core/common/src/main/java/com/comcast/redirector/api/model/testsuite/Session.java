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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.redirector.api.model.testsuite;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@XmlSeeAlso({Event.class, TestSuiteResponse.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class Session implements Serializable {
    @XmlAttribute(name = "id")
    private String id;

    @XmlElement(name = "events", type = Event.class)
    private List<Event> events = new ArrayList<>();

    @XmlElement(name = "actual")
    private TestSuiteResponse actual;

    public Session() {
    }

    public Session(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public TestSuiteResponse getActual() {
        return actual;
    }

    public void setActual(TestSuiteResponse actual) {
        this.actual = actual;
    }
}
