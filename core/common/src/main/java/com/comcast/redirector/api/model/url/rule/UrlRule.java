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

package com.comcast.redirector.api.model.url.rule;

import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.VisitableExpression;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Objects;

@XmlRootElement(name = "urlRule")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UrlRule extends VisitableExpression implements Serializable, Expressions {

    @XmlElement(name = "urn")
    private String urn;
    @XmlElement(name = "protocol")
    private String protocol;
    @XmlElement(name = "port")
    private String port;
    @XmlElement(name = "ipProtocolVersion")
    private String ipProtocolVersion;

    public String getUrn() {
        return urn;
    }

    public void setUrn(String urn) {
        this.urn = urn;
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

    public String getIpProtocolVersion() {
        return ipProtocolVersion;
    }

    public void setIpProtocolVersion(String ipProtocolVersion) {
        this.ipProtocolVersion = ipProtocolVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UrlRule urlRule = (UrlRule) o;
        return Objects.equals(urn, urlRule.urn) &&
                Objects.equals(protocol, urlRule.protocol) &&
                Objects.equals(port, urlRule.port) &&
                Objects.equals(ipProtocolVersion, urlRule.ipProtocolVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(urn, protocol, port, ipProtocolVersion);
    }
}
