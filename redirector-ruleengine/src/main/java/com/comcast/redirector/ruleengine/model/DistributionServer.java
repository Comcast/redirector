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
 */
package com.comcast.redirector.ruleengine.model;

import java.util.Objects;

public class DistributionServer extends Server {
    private double percent;

    public DistributionServer (Server server, double percent) {
        setPath(server.getPath());
        this.setDescription(server.getDescription());
        this.setName(server.getName());
        this.setURL(server.getURL());
        this.setSecureURL(server.getSecureURL());
        this.setReturnStatementType(server.getReturnStatementType());
        this.percent = percent;
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DistributionServer that = (DistributionServer) o;
        return Objects.equals(percent, that.percent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), percent);
    }
}
