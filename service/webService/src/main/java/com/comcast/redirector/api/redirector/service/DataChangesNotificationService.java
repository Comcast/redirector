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

package com.comcast.redirector.api.redirector.service;

import com.comcast.redirector.dataaccess.dao.IEmptyObjectDAO;
import com.comcast.redirector.dataaccess.dao.INodeVersionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataChangesNotificationService implements IDataChangesNotificationService {

    @Autowired
    private IEmptyObjectDAO triggerModelReloadDAO;

    @Autowired
    private IEmptyObjectDAO triggerStacksReloadDAO;

    @Autowired
    private INodeVersionDAO nodeVersionDAO;

    @Autowired
    private IEmptyObjectDAO servicesChangesNotificationDAO;

    @Override
    public void triggerModelReload(String serviceName) {
        triggerModelReloadDAO.save(serviceName);
    }

    @Override
    public void triggerStacksReload(String serviceName) {
        triggerStacksReloadDAO.save(serviceName);
    }

    @Override
    public long getStacksReloadVersion(String serviceName) {
        return nodeVersionDAO.getStacksReloadNodeVersion(serviceName);
    }

    @Override
    public long getModelVersion(String serviceName) {
        return nodeVersionDAO.getModelChangedVersion(serviceName);
    }

    @Override
    public long getNamespacedListsVersion() {
        return nodeVersionDAO.getNamespacedListsVersion();
    }

    @Override
    public long getStacksVersion() {
        return nodeVersionDAO.getStacksVersion();
    }

    @Override
    public void updateStacksVersion() {
        servicesChangesNotificationDAO.save();
    }
}
