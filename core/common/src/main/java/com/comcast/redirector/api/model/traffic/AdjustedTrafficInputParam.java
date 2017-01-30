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

package com.comcast.redirector.api.model.traffic;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "AdjustedTrafficInputParam")
@XmlAccessorType(XmlAccessType.FIELD)
public class AdjustedTrafficInputParam {

    public AdjustedTrafficInputParam() {
    }

    @XmlElement
    private String flavor = "";

    @XmlElement
    private long numberOfHostsToAdjust;

    @XmlElement
    private long adjustedThreshold;

    @XmlElement
    private long adjustedWeight;

    @XmlElement
    private long defaultWeight;

    public String getFlavor() {
        return flavor;
    }

    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }

    public long getNumberOfHostsToAdjust() {
        return numberOfHostsToAdjust;
    }

    public void setNumberOfHostsToAdjust(long numberOfHostsToAdjust) {
        this.numberOfHostsToAdjust = numberOfHostsToAdjust;
    }

    public long getAdjustedThreshold() {
        return adjustedThreshold;
    }

    public void setAdjustedThreshold(long adjustedThreshold) {
        this.adjustedThreshold = adjustedThreshold;
    }

    public long getAdjustedWeight() {
        return adjustedWeight;
    }

    public void setAdjustedWeight(long adjustedWeight) {
        this.adjustedWeight = adjustedWeight;
    }

    public long getDefaultWeight() {
        return defaultWeight;
    }

    public void setDefaultWeight(long defaultWeight) {
        this.defaultWeight = defaultWeight;
    }
}
