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

package com.comcast.redirector.core.balancer.serviceprovider.backup;

import com.comcast.redirector.api.model.xrestack.HostIPs;
import com.comcast.redirector.api.model.xrestack.StackData;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;

public class StackBackup {
    private int version;
    private List<StackSnapshot> snapshotList;

    public StackBackup() {
        this(0, new ArrayList<>());
    }

    public StackBackup(int version, List<StackSnapshot> snapshotList) {
        this.version = version;
        this.snapshotList = snapshotList;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public List<StackSnapshot> getSnapshotList() {
        if (snapshotList == null) {
            snapshotList = new ArrayList<>();
        }

        return snapshotList;
    }

    public void setSnapshotList(List<StackSnapshot> snapshotList) {
        this.snapshotList = snapshotList;
    }


    public void addSnapshot(StackSnapshot snapshot) {
        if (this.snapshotList == null) {
            snapshotList = new ArrayList<>();
        }

        List<StackSnapshot> tempSnapshotList = new ArrayList<>(snapshotList);

        for (StackSnapshot currSnapshot : tempSnapshotList) {
            if (currSnapshot == null) continue;
            if (compareSnapshots(snapshot, currSnapshot)) {
                List<StackSnapshot.Host> hosts = new ArrayList<>(currSnapshot.getHosts());
                hosts.removeAll(snapshot.getHosts());
                hosts.addAll(snapshot.getHosts());
                currSnapshot.setHosts(hosts);
                snapshotList = tempSnapshotList;
                return;
            }
        }
        snapshotList.add(snapshot);
    }

    public void deleteSnapshotIgnoringIPv6(StackSnapshot snapshotToDelete) {

        if (snapshotList != null) {
            List<StackSnapshot> currentSnapshotListCopy = new ArrayList<>(snapshotList);

            for (StackSnapshot currentSnapshot : currentSnapshotListCopy) {
                if (currentSnapshot == null) continue;
                if (compareSnapshots(snapshotToDelete, currentSnapshot)) {
                    List<StackSnapshot.Host> currentHosts = new ArrayList<>(currentSnapshot.getHosts());
                    if (currentHosts.size() > 1) {
                        filterOutHostsByIPv4(currentHosts, snapshotToDelete.getHosts());
                        currentSnapshot.setHosts(currentHosts);
                        snapshotList = currentSnapshotListCopy;
                    } else {
                        filterOutSnapshotByPathAndIPv4(snapshotList, snapshotToDelete);
                    }
                    break;
                }
            }
        }
    }

    private void filterOutHostsByIPv4(List<StackSnapshot.Host> currentHosts, List<StackSnapshot.Host> hostsRemove) {
        for (StackSnapshot.Host hostToRemove : hostsRemove) {
            Iterator<StackSnapshot.Host> iter = currentHosts.iterator();
            while (iter.hasNext()) {
                StackSnapshot.Host currentHost = iter.next();
                if (currentHost.getIpv4().equals(hostToRemove.getIpv4())) {
                    iter.remove();
                }
            }
        }
    }

    private void filterOutSnapshotByPathAndIPv4(List<StackSnapshot> snapshotListToFilter, StackSnapshot snapshotToDelete) {
        Iterator<StackSnapshot> iter = snapshotListToFilter.iterator();
        while (iter.hasNext()) {
            StackSnapshot stackSnapshot = iter.next();
            boolean pathsAreEqual = stackSnapshot.getPath().equals(snapshotToDelete.getPath());
            boolean hostsAreEqual = true;
            StackSnapshot.Host testHost = new StackSnapshot.Host();
            for (StackSnapshot.Host host : stackSnapshot.getHosts()) {
                testHost.setIpv4(host.getIpv4());
                testHost.setIpv6(null);
                if (!snapshotToDelete.getHosts().contains(testHost)) {
                    hostsAreEqual = false;
                }
            }
            if (pathsAreEqual && hostsAreEqual) {
                iter.remove();
                break;
            }
        }
    }

    public void syncStackSnapshot(final StackSnapshot stackSnapshotToSyncWith) {
        if (snapshotList != null) {

            // find current stack
            StackSnapshot snapshotToSync = null;
            for (StackSnapshot stackSnapshot : snapshotList) {
                if (stackSnapshot == null) {
                    continue;
                }
                if (stackSnapshot.getPath().equals(stackSnapshotToSyncWith.getPath())) {
                    snapshotToSync = stackSnapshot;
                    break;
                }
            }

            // remove stacks from current snapshot that are not present in stackSnapshotToSyncWith
            if (snapshotToSync != null && snapshotToSync.getHosts() != null) {
                List<StackSnapshot.Host> hosts = FluentIterable
                        .from(snapshotToSync.getHosts())
                        .filter(new Predicate<StackSnapshot.Host>() {
                            private StackSnapshot.Host IPv4 = new StackSnapshot.Host();

                            @Override
                            public boolean apply(StackSnapshot.Host input) {
                                IPv4.setIpv4(input.getIpv4());
                                IPv4.setIpv6(null);
                                return stackSnapshotToSyncWith.getHosts().contains(IPv4);
                            }
                        }).toList();

                if (CollectionUtils.isEmpty(hosts)) {
                    snapshotList.remove(snapshotToSync);
                }
                else {
                    snapshotToSync.setHosts(hosts);
                }
            }
        }
    }

    void deleteSnapshot(StackSnapshot snapshot) {
        if (snapshotList != null) {
            List<StackSnapshot> tempSnapshotList = new ArrayList<>(snapshotList);

            for (StackSnapshot currSnapshot : tempSnapshotList) {
                if (currSnapshot == null) {
                    continue;
                }
                if (compareSnapshots(snapshot, currSnapshot)) {
                    List<StackSnapshot.Host> hosts = new ArrayList<>(currSnapshot.getHosts());
                    if (hosts.size() > 1) {
                        hosts.removeAll(snapshot.getHosts());
                        currSnapshot.setHosts(hosts);
                        snapshotList = tempSnapshotList;
                    } else {
                        snapshotList.remove(currSnapshot);
                    }
                    break;
                }
            }
        }
    }

    private boolean compareSnapshots(StackSnapshot snapshot, StackSnapshot currSnapshot) {
       if (snapshot != null && currSnapshot != null) {
            String path = snapshot.getPath();
            String currPath = currSnapshot.getPath();
            if (path != null && currPath != null) {
                return path.equals(currPath);
            }
        }
        return false;
    }

    @JsonIgnore
    public Set<StackData> getAllStacks() {
        Set<StackData> data = new HashSet<>();
        for (StackSnapshot snapshot : getSnapshotList()) {
            if (snapshot == null) continue;
            data.add(
                    new StackData(
                            snapshot.getPath(),
                            Lists.transform(
                                snapshot.getHosts(),
                                new Function<StackSnapshot.Host, HostIPs>() {
                                    @Override
                                    public HostIPs apply(StackSnapshot.Host input) {
                                        return new HostIPs(input.getIpv4(), input.getIpv6(), input.getWeight());
                                    }
                                })));
        }

        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StackBackup that = (StackBackup) o;
        return Objects.equals(version, that.version) &&
                Objects.equals(snapshotList, that.snapshotList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, snapshotList);
    }
}
