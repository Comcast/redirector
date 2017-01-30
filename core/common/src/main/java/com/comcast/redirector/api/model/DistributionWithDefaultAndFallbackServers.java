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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */

package com.comcast.redirector.api.model;

import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.distribution.Rule;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.*;

@XmlRootElement(name="distributionWithDefaultAndFallbackServers")
@XmlSeeAlso({Server.class, Distribution.class, Rule.class})
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true) // TODO: we don't have fallback. Why do we need it in class name then?
public class DistributionWithDefaultAndFallbackServers implements java.io.Serializable, Expressions {
    @XmlElement(required = false)
    private Distribution distribution;

    @XmlElement(required = false)
    private Server defaultServer;


    public DistributionWithDefaultAndFallbackServers() {
    }

    public DistributionWithDefaultAndFallbackServers(Distribution distribution, Server defaultServer) {
        this.distribution = distribution;
        this.defaultServer = defaultServer;
    }

    public Distribution getDistribution() {
        return distribution;
    }

    public void setDistribution(Distribution distribution) {
        this.distribution = distribution;
    }

    public Server getDefaultServer() {
        return defaultServer;
    }

    public void setDefaultServer(Server defaultServer) {
        this.defaultServer = defaultServer;
    }
}
