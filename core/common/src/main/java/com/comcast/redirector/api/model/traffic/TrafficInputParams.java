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

package com.comcast.redirector.api.model.traffic;

import javax.xml.bind.annotation.*;
import java.util.*;

@XmlRootElement(name="trafficInputParams")
@XmlSeeAlso({AdjustedTrafficInputParam.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class TrafficInputParams {

    public TrafficInputParams() {
    }

    public enum HostsMode {
        ONLY_ACTIVE_WHITELISTED,
        ALL_WHITELISTED
    }

    public enum DistributionMode {
        CURRENT,
        NEXT
    }

    @XmlElement
    private AdjustedTrafficInputParams adjustedTrafficInputParams = new AdjustedTrafficInputParams();

    @XmlElement
    private long connectionThreshold;

    @XmlElement
    private long totalNumberConnections;

    @XmlElement
    private HostsMode hostsMode = HostsMode.ALL_WHITELISTED;

    @XmlElement
    private DistributionMode distributionMode = DistributionMode.CURRENT;

    public AdjustedTrafficInputParams getAdjustedTrafficInputParams() {
        return adjustedTrafficInputParams;
    }

    public void setAdjustedTrafficInputParams(AdjustedTrafficInputParams adjustedTrafficInputParams) {
        this.adjustedTrafficInputParams = adjustedTrafficInputParams;
    }

    public AdjustedTrafficInputParam getAdjustedTrafficInputParams(String flavor) {

        return getDistribution()
                .stream()
                .filter(param -> flavor.equals(param.getFlavor()))
                .findFirst()
                .orElseGet(() -> flavor.equals(getDefaultServer().getFlavor()) ? getDefaultServer() : null);
    }

    public AdjustedTrafficInputParam getDefaultServer() {
        return adjustedTrafficInputParams.getDefaultServer();
    }

    public void setDefaultServer(AdjustedTrafficInputParam defaultServer) {
        adjustedTrafficInputParams.setDefaultServer(defaultServer);
    }

    public List<AdjustedTrafficInputParam> getDistribution() {
        return adjustedTrafficInputParams.getDistribution();
    }

    public void setDistribution(List<AdjustedTrafficInputParam> distribution) {
        adjustedTrafficInputParams.setDistribution(distribution);
    }

    public long getConnectionThreshold() {
        return connectionThreshold;
    }

    public void setConnectionThreshold(long connectionThreshold) {
        this.connectionThreshold = connectionThreshold;
    }

    public long getTotalNumberConnections() {
        return totalNumberConnections;
    }

    public void setTotalNumberConnections(long totalNumberConnections) {
        this.totalNumberConnections = totalNumberConnections;
    }

    public HostsMode getHostsMode() {
        return hostsMode;
    }

    public void setHostsMode(HostsMode hostsMode) {
        this.hostsMode = hostsMode;
    }

    public DistributionMode getDistributionMode() {
        return distributionMode;
    }

    public void setDistributionMode(DistributionMode distributionMode) {
        this.distributionMode = distributionMode;
    }

    public AdjustedTrafficInputParams.AdjustedTrafficCalculationMode getAdjustedTrafficCalculationMode() {
        return adjustedTrafficInputParams.getAdjustedTrafficCalculationMode();
    }

    public void setAdjustedTrafficCalculationMode(AdjustedTrafficInputParams.AdjustedTrafficCalculationMode adjustedTrafficCalculationMode) {
        adjustedTrafficInputParams.setAdjustedTrafficCalculationMode(adjustedTrafficCalculationMode);
    }
}
