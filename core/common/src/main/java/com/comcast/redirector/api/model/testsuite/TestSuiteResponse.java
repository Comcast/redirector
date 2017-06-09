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

package com.comcast.redirector.api.model.testsuite;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class TestSuiteResponse {

    @XmlElement(name = "protocol")
    private String protocol;

    @XmlElement(name = "port")
    private String port;

    @XmlElement(name = "ipVersion")
    private String ipVersion;

    @XmlElement(name = "urn")
    private String urn;

    @XmlElement(name = "xreStack")
    private String xreStack;

    @XmlElement(name = "flavor")
    private String flavor;

    @XmlElement(name = "rule")
    private String rule;

    @XmlElement(name = "appliedUrlRules")
    private Set<String> appliedUrlRules;

    @XmlElement(name = "responseType")
    private String responseType;

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

    public String getIpVersion() {
        return ipVersion;
    }

    public void setIpVersion(String ipVersion) {
        this.ipVersion = ipVersion;
    }

    public String getUrn() {
        return urn;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }

    public String getXreStack() {
        return xreStack;
    }

    public void setXreStack(String xreStack) {
        this.xreStack = xreStack;
    }

    public String getFlavor() {
        return flavor;
    }

    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public Set<String> getAppliedUrlRules() {
        return appliedUrlRules;
    }

    public void setAppliedUrlRules(Set<String> appliedUrlRules) {
        this.appliedUrlRules = appliedUrlRules;
    }

    public void addAppliedRuleName(String applideRuleName) {
        if (this.appliedUrlRules == null) {
            this.appliedUrlRules = new HashSet<>();
        }
        this.appliedUrlRules.add(applideRuleName);
    }

    /**
     * Check if all non-empty fields of expected response equal to fields of actual one.
     * Note: comparison is case-insensitive
     *
     * @param other test case result to compare with
     * @return true if fields match
     */
    public boolean matches(TestSuiteResponse other) {
        return  isNotEmpty()
                && ownParameterAbsentOrEqualsOther(protocol, other.getProtocol())
                && ownParameterAbsentOrEqualsOther(port, other.getPort())
                && ownParameterAbsentOrEqualsOther(ipVersion, other.getIpVersion())
                && ownParameterAbsentOrEqualsOther(urn, other.getUrn())
                && ownParameterAbsentOrEqualsOther(xreStack, other.getXreStack())
                && ownParameterAbsentOrEqualsOther(flavor, other.getFlavor())
                && ownParameterAbsentOrEqualsOther(rule, other.getRule())
                && ownAppliedUrlRulesAreSubsetOfOther(appliedUrlRules, other.getAppliedUrlRules())
                && ownParameterAbsentOrEqualsOther(responseType, other.getResponseType());
    }

    private boolean isNotEmpty() {
        return !(new TestSuiteResponse().equals(this));
    }

    private static boolean ownParameterAbsentOrEqualsOther(String own, String other) {
        return StringUtils.isBlank(own) || own.equalsIgnoreCase(other);
    }

    private static boolean ownAppliedUrlRulesAreSubsetOfOther(Set<String> own, Set<String> other) {
        return CollectionUtils.isEmpty(own) || (CollectionUtils.isNotEmpty(other) && other.containsAll(own));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestSuiteResponse that = (TestSuiteResponse) o;
        return Objects.equals(protocol, that.protocol) &&
                Objects.equals(port, that.port) &&
                Objects.equals(ipVersion, that.ipVersion) &&
                Objects.equals(urn, that.urn) &&
                Objects.equals(xreStack, that.xreStack) &&
                Objects.equals(flavor, that.flavor) &&
                Objects.equals(rule, that.rule) &&
                Objects.equals(appliedUrlRules, that.appliedUrlRules) &&
                Objects.equals(responseType, that.responseType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocol, port, ipVersion, urn, xreStack, flavor, rule, appliedUrlRules, responseType);
    }

    @Override
    public  String toString() {
        return "protocol: " + protocol + ", port: " + port + ", ipVersion: " + ipVersion + ", urn: " + urn + ", xreStack: " +
                xreStack +  ", flavor: " + flavor + ", rule: " + rule + ", appliedUrlRules: " + appliedUrlRules + ", responseType: " + responseType + ", protocol: " + protocol;
    }
}
