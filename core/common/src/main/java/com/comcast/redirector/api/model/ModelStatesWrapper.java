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
package com.comcast.redirector.api.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Set;

@XmlRootElement(name = "modelStates")
public class ModelStatesWrapper {

    private Set<ModelState> modelStates = new HashSet<>();

    public ModelStatesWrapper() {

    }

    public ModelStatesWrapper(Set<ModelState> modelStates) {
        this.modelStates = modelStates;
    }

    public void add (ModelState modelState) {
        this.modelStates.add(modelState);
    }

    public Set<ModelState> getModelStates() {
        return modelStates;
    }

    public void setModelStates(Set<ModelState> modelStates) {
        this.modelStates = modelStates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModelStatesWrapper that = (ModelStatesWrapper) o;

        return modelStates != null ? modelStates.equals(that.modelStates) : that.modelStates == null;

    }

    @Override
    public int hashCode() {
        return modelStates != null ? modelStates.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ModelStatesWrapper{" +
                "modelStates=" + modelStates +
                '}';
    }
}
