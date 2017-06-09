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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.endpoint.http.model;

import javax.xml.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement(name = "location")
@XmlSeeAlso({Location.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class Location {

    @XmlElement(name = "destination")
    private String destination = "";

    @XmlElement(name = "description")
    private String description = "";

    @XmlElement(name = "protocol")
    private String protocol = "";

    @XmlElement(name = "port")
    private String port = "";

    @XmlElement(name = "context")
    private Map<String, String> context = new HashMap<>();

    public Location() {}

    public Location(String destination, String description, String protocol) {
        this.destination = destination;
        this.description = description;
        this.protocol = protocol;
    }

    public Location(String destination, String description, String protocol, String port) {
        this(destination, description, protocol);
        this.port = port;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public Map<String, String> getContext() {
        return context;
    }

    public void setContext(Map<String, String> context) {
        this.context = context;
    }
}
