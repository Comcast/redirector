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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.redirector.api.model.traffic;

import com.comcast.redirector.api.model.weightcalculator.AdjustedThreshold;
import com.comcast.redirector.api.model.weightcalculator.AdjustedWeights;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class TrafficStatsItem implements java.io.Serializable {

    @XmlAttribute(name = "title")
    private String title;

    @XmlElement(name = "flavor")
    private String flavor;

    @XmlAttribute(name = "numberConnections")
    private long numberConnections;

    @XmlAttribute(name = "totalConnectionLimit")
    private long totalConnectionLimit;

    @XmlAttribute(name = "connectionsHostRatio")
    private long connectionsHostRatio;

    @XmlElement(name="adjustedWeights")
    private AdjustedWeights adjustedWeights;

    @XmlElement(name = "adjustedThreshold")
    private AdjustedThreshold adjustedThreshold;

    public TrafficStatsItem() {
    }

    public String getTitle() {
        return title;
    }

    public String getFlavor() {
        return flavor;
    }

    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getNumberConnections() {
        return numberConnections;
    }

    public void setNumberConnections(long numberConnections) {
        this.numberConnections = numberConnections;
    }

    public long getTotalConnectionLimit() {
        return totalConnectionLimit;
    }

    public void setTotalConnectionLimit(long totalConnectionLimit) {
        this.totalConnectionLimit = totalConnectionLimit;
    }

    public long getConnectionsHostRatio() {
        return connectionsHostRatio;
    }

    public void setConnectionsHostRatio(long connectionsHostRatio) {
        this.connectionsHostRatio = connectionsHostRatio;
    }

    public AdjustedWeights getAdjustedWeights() {
        return adjustedWeights;
    }

    public void setAdjustedWeights(AdjustedWeights adjustedWeights) {
        this.adjustedWeights = adjustedWeights;
    }

    public AdjustedThreshold getAdjustedThreshold() {
        return adjustedThreshold;
    }

    public void setAdjustedThreshold(AdjustedThreshold adjustedThreshold) {
        this.adjustedThreshold = adjustedThreshold;
    }
}
