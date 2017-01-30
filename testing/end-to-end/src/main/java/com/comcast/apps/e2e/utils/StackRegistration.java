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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.apps.e2e.utils;

import com.comcast.apps.e2e.config.E2EConfigLoader;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackBackup;
import com.comcast.redirector.core.balancer.serviceprovider.backup.StackSnapshot;
import com.comcast.xre.common.redirector.v2.utils.Host;
import com.comcast.xre.common.redirector.v2.utils.HostRegister;
import com.comcast.redirector.common.util.StacksHelper;

import java.util.List;
import java.util.stream.Collectors;

public class StackRegistration {

    private final StackBackup stackBackup;
    private final HostRegister hostRegister;

    public StackRegistration(StackBackup stackBackup) {
        this.stackBackup = stackBackup;

        String zooKeeperConnection = E2EConfigLoader.getDefaultInstance().getZooKeeperConnection();
        String zookeeperBasePath = E2EConfigLoader.getDefaultInstance().getZooKeeperBasePath();

        hostRegister = new HostRegister(zooKeeperConnection, zookeeperBasePath);
    }

    public void registerStacks() {
        List<StackSnapshot> stackSnapshots = stackBackup.getSnapshotList();
        for (StackSnapshot stackSnapshot : stackSnapshots) {
            List<Host> hosts = stackSnapshot.getHosts().stream()
                .map(host -> new Host(host.getIpv4(), host.getIpv6(), host.getWeight()))
                .collect(Collectors.toList());

            String fullPath = stackSnapshot.getPath();
            final String serviceName = StacksHelper.getServiceName(fullPath);

            String stackPathWithoutServiceName = StacksHelper.getStackPathWithoutServiceName(stackSnapshot.getPath());
            final String flavor = StacksHelper.getFlavorPath(stackPathWithoutServiceName);
            String stackName = StacksHelper.getStackPath(stackPathWithoutServiceName);

            hostRegister.registerHosts(stackName, flavor, serviceName, hosts);
        }
    }

    // TODO: due to limits of current impl this method can't be called. So remove limits and call this method at the end
    public void deRegisterStacks() {
        hostRegister.deRegisterAllHosts();
    }
}
