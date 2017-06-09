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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package com.comcast.redirector.ruleengine.model;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import java.util.*;

public class UrlParams extends LanguageElement {
    private Integer port = null;
    private String urn = null;
    private String protocol = null;
    private Integer ipProtocolVersion = null;
    private Set<String> appliedRulesNames = null;

    public UrlParams() {
    }

    public UrlParams(String protocol, String urn, int port, int ipProtocolVersion) {
        super();
        this.urn = urn;
        this.protocol = protocol;
        this.port = port;
        this.ipProtocolVersion = ipProtocolVersion;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

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

    public Integer getIPProtocolVersion() {
        return ipProtocolVersion;
    }

    public void setIPProtocolVersion(Integer ipProtocolVersion) {
        this.ipProtocolVersion = ipProtocolVersion;
    }

    public Set<String> getAppliedRulesNames() {
        return appliedRulesNames;
    }

    public void setAppliedRulesNames(Set<String> appliedRulesNames) {
        this.appliedRulesNames = appliedRulesNames;
    }

    public void addAppliedRuleName(String applideRuleName) {
        if (this.appliedRulesNames == null) {
            this.appliedRulesNames = new HashSet<>();
        }
        this.appliedRulesNames.add(applideRuleName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UrlParams urlParams = (UrlParams) o;
        return Objects.equals(port, urlParams.port) &&
                Objects.equals(urn, urlParams.urn) &&
                Objects.equals(protocol, urlParams.protocol) &&
                Objects.equals(ipProtocolVersion, urlParams.ipProtocolVersion) &&
                Objects.equals(name, urlParams.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(port, urn, protocol, ipProtocolVersion, name);
    }

    @Override
    protected void init(Element element) {
        List<Element> children = getChildElements(element);
        for (Element e : children) {
            String tagName = e.getTagName();
            String value = e.getTextContent().trim();
            switch (tagName) {
                case UrlRuleDefaultStatement.TAG_URN:
                    urn = value;
                    break;
                case UrlRuleDefaultStatement.TAG_PORT:
                    if (!"".equals(value)) {
                        port = Integer.parseInt(value);
                    }
                    break;
                case UrlRuleDefaultStatement.TAG_PROTOCOL:
                    protocol = value;
                    break;
                case UrlRuleDefaultStatement.TAG_IP_PROTOCOL_VERSION:
                    if (!"".equals(value)) {
                        ipProtocolVersion = Integer.parseInt(value);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown the tag name: " + tagName);

            }
        }
    }

    public boolean allItemsFilled() {
        if (StringUtils.isNotEmpty(getUrn()) && StringUtils.isNotEmpty(getProtocol())
                && ((getPort() != null) && (getPort() != 0)) && ((getIPProtocolVersion() != null) && (getPort() != 0))) {

                return true;
        }
        return false;
    }

    @Override
    public String toString(int indent, Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("Protocol: [ ");
        sb.append(protocol);
        sb.append(" ]");
        sb.append(" URN: [ ");
        sb.append(urn);
        sb.append(" ]");
        sb.append(" Port: [ ");
        sb.append(port);
        sb.append(" ]");
        sb.append(" ipProtocolVersion: [ ");
        sb.append(ipProtocolVersion);
        sb.append(" ]");

        return sb.toString();
    }

    public enum IpProtocolVersion {
        IPV4("ipv4Address"),
        IPV6("ipv6Address");

        private String id;

        IpProtocolVersion(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return id;
        }
    }
}

