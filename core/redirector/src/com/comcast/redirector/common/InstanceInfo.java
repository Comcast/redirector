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
package com.comcast.redirector.common;

import com.comcast.redirector.common.util.PathUtils;
import com.comcast.redirector.ruleengine.model.Server;
import com.comcast.redirector.ruleengine.model.ServerGroup;
import com.comcast.redirector.ruleengine.model.UrlParams;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;

import java.util.Set;

/**
 * Created by jgovin200 on 7/11/14.
 */
// TODO: variables and types with names ending with Info, Data etc is not the best practice. So rename this class
public class InstanceInfo {

    private Server server;
    private String flavor;
    private String stack;
    private String serverIp;
    private String address;
    private String serverIpV6;
    private String url;
    private Boolean isStackBased;
    private Boolean isAdvancedRule;
    private ServerGroup serverGroup;
    private String urn;
    private int ipProtocolVersion;
    private String protocol;
    private int port;
    private String ruleName;
    private Set<String> appliedUrlRules;

    private static final String DEFAULT = "Default";

    public InstanceInfo(ServerGroup serverGroup) {
        this.serverGroup = serverGroup;
        defineRuleName();
    }

    public InstanceInfo(Server server,
                        String stack,
                        String serverIp,
                        String serverIpV6,
                        Boolean isStackBased) {
        this.server = server;
        this.flavor = PathUtils.getFlavorFromXREStackPath(server.getPath());
        this.stack = stack;
        this.serverIp = serverIp;
        this.serverIpV6 = serverIpV6;
        this.isStackBased = isStackBased;
        this.isAdvancedRule = false;
        address = serverIp;
        url = server.getURL();
        defineRuleName();
    }

    public InstanceInfo(Server server, String advancedUrl) {
        this.server = server;
        this.flavor = null;
        this.stack = null;
        this.serverIp = null;
        this.serverIpV6 = null;
        this.isStackBased = false;
        this.isAdvancedRule = true;
        this.url = advancedUrl;
        defineRuleName();
    }

    private void defineRuleName() {
        if (serverGroup != null) {
            ruleName = serverGroup.getServers().get(0).getName();
        } else {
            ruleName = server.getName();
        }
        if (this.ruleName.contains(InstanceInfo.DEFAULT))
            this.ruleName = InstanceInfo.DEFAULT;
    }

    public String getFlavor() {
        return flavor;
    }

    public String getStack() {
        return stack;
    }

    public String getAddress() {
        return address;
    }

    public String getUrl() {
        return url;
    }

    public Server getServer() { return server; }

    public String getServerIp() {
        return serverIp;
    }

    public String getServerIpV6() {
        return serverIpV6;
    }

    public Boolean isStackBased() { return isStackBased; }

    public Boolean getIsAdvancedRule() { return isAdvancedRule; }

    public boolean isServerGroup() {
        return serverGroup != null;
    }

    public ServerGroup getServerGroup() {
        return serverGroup;
    }

    public String getUrn() {
        return urn;
    }

    public int getIpProtocolVersion() {
        return ipProtocolVersion;
    }

    public String getProtocol() {
        return protocol;
    }

    public int getPort() {
        return port;
    }

    public Set<String> getAppliedUrlRules() {
        return appliedUrlRules;
    }

    public boolean isNeedUrlParams() {
        return  url != null && (url.contains(RedirectorConstants.HOST_PLACEHOLDER)
                                || url.contains(RedirectorConstants.PORT_PLACEHOLDER)
                                || url.contains(RedirectorConstants.PROTOCOL_PLACEHOLDER)
                                || url.contains(RedirectorConstants.URN_PLACEHOLDER));
    }

    public boolean isValid(UrlParams urlParams) {
        boolean invalidUrlParams = isNeedUrlParams()
                && ( urlParams.getIPProtocolVersion() == IpProtocolVersion.IPV6.getVersion() && StringUtils.isBlank(serverIpV6)
                     || urlParams.getIPProtocolVersion() == IpProtocolVersion.IPV4.getVersion() && StringUtils.isBlank(serverIp));

        return ! invalidUrlParams;
    }

    public void replaceUrlParams(UrlParams urlParams) {
        if (url.contains(RedirectorConstants.HOST_PLACEHOLDER) && urlParams.getIPProtocolVersion() != null) {
            // TODO: move to separate place
            if (urlParams.getIPProtocolVersion() == IpProtocolVersion.IPV6.getVersion() && StringUtils.isNotBlank(serverIpV6)) {
                if (InetAddressValidator.getInstance().isValidInet6Address(serverIpV6)) {
                    url = url.replace(RedirectorConstants.HOST_PLACEHOLDER, "[" + serverIpV6 + "]");
                } else {
                    url = url.replace(RedirectorConstants.HOST_PLACEHOLDER, serverIpV6);
                }
                address = serverIpV6;
                ipProtocolVersion = IpProtocolVersion.IPV6.getVersion();
            } else if (StringUtils.isNotBlank(serverIp)) {
                url = url.replace(RedirectorConstants.HOST_PLACEHOLDER, serverIp);
                ipProtocolVersion = IpProtocolVersion.IPV4.getVersion();;
            }
        }

        if (urlParams.getPort() != null) {
            port = urlParams.getPort();
            url = url.replace(RedirectorConstants.PORT_PLACEHOLDER, urlParams.getPort().toString());
        }

        if (urlParams.getProtocol() != null) {
            protocol = urlParams.getProtocol();
            url = url.replace(RedirectorConstants.PROTOCOL_PLACEHOLDER, urlParams.getProtocol());
        }

        if (urlParams.getUrn() != null) {
            urn = urlParams.getUrn();
            url = url.replace(RedirectorConstants.URN_PLACEHOLDER, urlParams.getUrn());
        }

        if (urlParams.getAppliedRulesNames() != null) {
            appliedUrlRules = urlParams.getAppliedRulesNames();
        }
    }

    @Override
    public String toString() {
        return "InstanceInfo{" +
                "server=" + server +
                ", flavorRule='" + flavor + '\'' +
                ", urlRule='" + appliedUrlRules + '\'' +
                ", stack='" + stack + '\'' +
                ", serverIp='" + serverIp + '\'' +
                ", address='" + address + '\'' +
                ", serverIpV6='" + serverIpV6 + '\'' +
                ", url='" + url + '\'' +
                ", isStackBased=" + isStackBased +
                ", isAdvancedRule=" + isAdvancedRule +
                '}';
    }

    public String getRuleName() {
        return ruleName;
    }

    public boolean isSuccessful() {
        return isServerGroup() || StringUtils.isNotBlank(getUrl()) && !isNeedUrlParams();
    }

    public void appendConnectionUrlToQuery(String connectUrl) {
        String query = getQueryString(connectUrl);
        appendUrlQuery(query);
    }

    public void appendUrlQuery(String query) {
        if (StringUtils.isNotBlank(query)) {
            if (StringUtils.isNotBlank(getQueryString(url))) {
                url += "&" + query;
            } else {
                url += "?" + query;
            }
        }
    }

    private String getQueryString(String url) {
        return StringUtils.substringAfter(url, "?");
    }
}
