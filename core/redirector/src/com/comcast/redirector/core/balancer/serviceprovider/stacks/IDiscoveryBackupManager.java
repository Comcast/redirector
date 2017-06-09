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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package com.comcast.redirector.core.balancer.serviceprovider.stacks;

import com.comcast.redirector.core.balancer.serviceprovider.backup.StackBackup;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackSnapshot;

public interface IDiscoveryBackupManager {
    void addStackSnapshot(StackSnapshot snapshot);
    void deleteStackSnapshot(StackSnapshot snapshot);

    /**
     * currently this method is called once {@link org.apache.curator.x.discovery.details.CustomServiceCache}
     * has been initialized
     *
     * in case some stacks became inactive while XRE Redirector was not running we need to perform sync of stacks backup
     * the sync process is that to remove stacks that are no longer active, no more actions performed here
     *
     * updating backup with new stacks that was registered while XRE Redirector was not running will be initiated by
     * {@link com.comcast.xre.common.redirector.v2.discovery.ZookeeperServiceDiscovery}
     *
     * @param snapshotToSycWith - contains list of stacks that are registered at the moment of
     * {@link org.apache.curator.x.discovery.details.CustomServiceCache} initialization
     */
    void syncStackSnapshot(StackSnapshot snapshotToSycWith);

    StackBackup getCurrentSnapshot();
}
