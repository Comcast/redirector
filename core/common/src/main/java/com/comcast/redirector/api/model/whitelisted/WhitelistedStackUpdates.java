/**
 * Copyright 2016 Comcast Cable Communications Management, LLC
 * <p>
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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package com.comcast.redirector.api.model.whitelisted;


import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.xrestack.PathItem;
import com.comcast.redirector.api.model.xrestack.ServicePaths;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@XmlRootElement(name = "whitelistedUpdates")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({WhitelistUpdate.class})
public class WhitelistedStackUpdates implements Serializable, Expressions {

    public WhitelistedStackUpdates() {
    }

    @XmlElementWrapper(name = "paths")
    private Map<String, WhitelistUpdate> whitelistedUpdates = new LinkedHashMap<>();

    public Map<String, WhitelistUpdate> getWhitelistedUpdates() {
        return whitelistedUpdates;
    }

    public void setWhitelistedUpdates(Map<String, WhitelistUpdate> whitelistedUpdates) {
        this.whitelistedUpdates = whitelistedUpdates;
    }

    public void addUpdateItemToAllFlavors(WhitelistUpdate whitelistUpdate, ServicePaths servicePaths, String serviceName) {
        addUpdateItem(whitelistUpdate, whitelistUpdate.getPath());
        for (PathItem pathItem : servicePaths.getPaths(serviceName).getStacks()) {
            if (pathItem.getValue().contains(whitelistUpdate.getPath())) {
                if (whitelistedUpdates.containsKey(pathItem.getValue())) {
                    whitelistedUpdates.get(pathItem.getValue()).setUpdated(whitelistUpdate.getUpdated());
                    whitelistedUpdates.get(pathItem.getValue()).setAction(whitelistUpdate.getAction());
                } else {
                    WhitelistUpdate whitelistUpdateForStackAndFlavor = new WhitelistUpdate();
                    whitelistUpdateForStackAndFlavor.setAction(whitelistUpdate.getAction());
                    whitelistUpdateForStackAndFlavor.setPath(pathItem.getValue());
                    whitelistUpdateForStackAndFlavor.setUpdated(whitelistUpdate.getUpdated());
                    whitelistedUpdates.put(pathItem.getValue(), whitelistUpdate);
                }
            }
        }
    }

    public void addUpdateItem(WhitelistUpdate whitelistUpdate, String key) {
        if (whitelistedUpdates.containsKey(key)) {
            whitelistedUpdates.get(whitelistUpdate.getPath()).setUpdated(whitelistUpdate.getUpdated());
            whitelistedUpdates.get(whitelistUpdate.getPath()).setAction(whitelistUpdate.getAction());
        } else {
            whitelistedUpdates.put(whitelistUpdate.getPath(), whitelistUpdate);
        }
    }
}
