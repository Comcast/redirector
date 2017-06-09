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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.api.model.xrestack;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@XmlRootElement(name = "servicePaths")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({Paths.class})
public class ServicePaths {
    private List<Paths> paths;

    public ServicePaths() {
        this(new ArrayList<Paths>());
    }

    public ServicePaths(List<Paths> paths) {
        this.paths = paths;
    }

    public List<Paths> getPaths() {
        return paths;
    }

    public Paths getPaths(String serviceName) {
        for (Paths path : this.paths) {
            if(serviceName.equals(path.getServiceName())) {
                return path;
            }
        }
        return new Paths(serviceName);
    }

    public void setPaths(List<Paths> paths) {
        this.paths = paths;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServicePaths that = (ServicePaths) o;
        return Objects.equals(paths, that.paths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paths);
    }
}
