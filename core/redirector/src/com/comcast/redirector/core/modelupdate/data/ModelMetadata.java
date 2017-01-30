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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.core.modelupdate.data;

import com.comcast.redirector.common.RedirectorConstants;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "modelMetadata")
public class ModelMetadata {
    private int version = RedirectorConstants.NO_MODEL_NODE_VERSION;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModelMetadata that = (ModelMetadata) o;

        if (version != that.version) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return version;
    }
}
