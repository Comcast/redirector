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

package com.comcast.redirector.api.model.weightcalculator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "adjustedThreshold")
@XmlAccessorType(XmlAccessType.FIELD)
public class AdjustedThreshold {

    @XmlElement
    String flavor;

    @XmlElement(name="adjustedThreshold")
    private double adjustedThreshold;

    @XmlElement(name="adjustedWeightedHostsTraffic")
    private long adjustedWeightedHostsTraffic;

    @XmlElement(name="adjustedWeightedHostPossibleTraffic")
    private long adjustedWeightedHostPossibleTraffic;

    @XmlElement(name="defaultWeightedHostsTraffic")
    private long defaultWeightedHostsTraffic;

    @XmlElement(name="defaultWeightedHostPossibleTraffic")
    private long defaultWeightedHostPossibleTraffic;

    public AdjustedThreshold() {
    }

    public AdjustedThreshold(double adjustedTraffic, long adjustedWeightedHostsTraffic, long adjustedWeightedHostPossibleTraffic, long defaultWeightedHostsTraffic, long defaultWeightedHostPossibleTraffic) {
        this.adjustedThreshold = adjustedTraffic;
        this.adjustedWeightedHostsTraffic = adjustedWeightedHostsTraffic;
        this.adjustedWeightedHostPossibleTraffic = adjustedWeightedHostPossibleTraffic;
        this.defaultWeightedHostsTraffic = defaultWeightedHostsTraffic;
        this.defaultWeightedHostPossibleTraffic = defaultWeightedHostPossibleTraffic;
    }

    public String getFlavor() {
        return flavor;
    }

    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }

    public double getAdjustedThreshold() {
        return adjustedThreshold;
    }

    public void setAdjustedThreshold(double adjustedThreshold) {
        this.adjustedThreshold = adjustedThreshold;
    }

    public long getAdjustedWeightedHostsTraffic() {
        return adjustedWeightedHostsTraffic;
    }

    public void setAdjustedWeightedHostsTraffic(long adjustedWeightedHostsTraffic) {
        this.adjustedWeightedHostsTraffic = adjustedWeightedHostsTraffic;
    }

    public long getDefaultWeightedHostsTraffic() {
        return defaultWeightedHostsTraffic;
    }

    public void setDefaultWeightedHostsTraffic(long defaultWeightedHostsTraffic) {
        this.defaultWeightedHostsTraffic = defaultWeightedHostsTraffic;
    }

    public long getAdjustedWeightedHostPossibleTraffic() {
        return adjustedWeightedHostPossibleTraffic;
    }

    public void setAdjustedWeightedHostPossibleTraffic(long adjustedWeightedHostPossibleTraffic) {
        this.adjustedWeightedHostPossibleTraffic = adjustedWeightedHostPossibleTraffic;
    }

    public long getDefaultWeightedHostPossibleTraffic() {
        return defaultWeightedHostPossibleTraffic;
    }

    public void setDefaultWeightedHostPossibleTraffic(long defaultWeightedHostPossibleTraffic) {
        this.defaultWeightedHostPossibleTraffic = defaultWeightedHostPossibleTraffic;
    }

    @Override
    public String toString() {
        return "AdjustedThreshold{" +
                "flavor='" + flavor + '\'' +
                ", adjustedThreshold=" + adjustedThreshold +
                ", adjustedWeightedHostsTraffic=" + adjustedWeightedHostsTraffic +
                ", adjustedWeightedHostPossibleTraffic=" + adjustedWeightedHostPossibleTraffic +
                ", defaultWeightedHostsTraffic=" + defaultWeightedHostsTraffic +
                ", defaultWeightedHostPossibleTraffic=" + defaultWeightedHostPossibleTraffic +
                '}';
    }
}
