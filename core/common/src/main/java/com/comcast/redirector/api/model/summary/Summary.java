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

package com.comcast.redirector.api.model.summary;


import javax.xml.bind.annotation.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Set;

@XmlRootElement(name = "summary")
@XmlSeeAlso({RowSummary.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class Summary implements java.io.Serializable {


    @XmlElement(name = "defaultServer")
    private RowSummary defaultServer;

    @XmlElement(name = "distributions", type = RowSummary.class)
    private List<RowSummary> distributions;

    @XmlElement(name = "rules", nillable = true, type = RowSummary.class)
    private Set<RowSummary> rules;

    public RowSummary getDefaultServer() {
        return defaultServer;
    }

    public void setDefaultServer(RowSummary defaultServer) {
        this.defaultServer = defaultServer;
    }

    public List<RowSummary> getDistributions() {
        return distributions;
    }

    public void setDistributions(List<RowSummary> distributions) {
        this.distributions = distributions;
    }

    public Set<RowSummary> getRules() {
        return rules;
    }

    public void setRules(Set<RowSummary> rules) {
        this.rules = rules;
    }

    private void writeObject(ObjectOutputStream stream)
            throws IOException {
        stream.defaultWriteObject();
    }

    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
    }
}
