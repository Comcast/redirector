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

package com.comcast.redirector.core.balancer.serviceprovider.stacks;

import com.comcast.redirector.api.model.xrestack.*;
import com.comcast.redirector.common.thread.ThreadUtils;
import com.comcast.redirector.common.util.ThreadLocalLogger;
import com.comcast.redirector.dataaccess.client.RedirectorDataSourceException;
import com.comcast.redirector.dataaccess.facade.ICommonModelFacade;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DataSourceStacksSupplier implements Supplier<Set<StackData>> {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(DataSourceStacksSupplier.class);

    private String appName;
    private ICommonModelFacade commonDaoFacade;
    private HostsFinder hostsFinder;

    public DataSourceStacksSupplier(ICommonModelFacade commonDaoFacade, String appName) {
        this.commonDaoFacade = commonDaoFacade;
        this.appName = appName;
        hostsFinder = new HostsFinder(this::getHostsForPath);
    }

    private List<HostIPs> getHostsForPath(XreStackPath path) {
        try {
            return new ArrayList(commonDaoFacade.getHosts(path));
        } catch (RedirectorDataSourceException e) {
            log.error("failed to get hosts for path " + path.getPath(), e);
            return null;
        }
    }

    @Override
    public Set<StackData> get() {
        long startMillis = System.currentTimeMillis(); // TODO: use AOP for logging of duration since it's cross-cutting concept
        log.info("Start getting data from zk - startTime=" + startMillis);

        Set<StackData> results = hostsFinder.getStacksWithHosts(getAllStackPathsForCurrentApp());

        long endMillis = System.currentTimeMillis();
        log.info("End getting data from zk - endTime=" + endMillis  + ", total duration=" + (endMillis - startMillis) + " millis");

        return results;
    }

    private Set<XreStackPath> getAllStackPathsForCurrentApp() {
        try {
            return commonDaoFacade.getAllStackPaths().stream()
                .filter(stackPath -> stackPath.getServiceName().equals(appName))
                .collect(Collectors.toSet());
        } catch (RedirectorDataSourceException e) {
            log.error("failed to get stacks", e);
            return Collections.emptySet();
        }
    }

    private static class HostsFinder {

        private static ExecutorService loadersPool =
            new ThreadPoolExecutor(10, 100, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), ThreadUtils.newThreadFactory("StacksChangeObserver.HostsFinder"));

        private Function<XreStackPath, List<HostIPs>> loadHostsForPath;

        HostsFinder(Function<XreStackPath, List<HostIPs>> loadHostsForPath) {
            this.loadHostsForPath = loadHostsForPath;
        }

        Set<StackData> getStacksWithHosts(Set<XreStackPath> paths) {
            return merge(load(paths));
        }

        private Collection<Future<StackData>> load(Collection<XreStackPath> paths) {
            Collection<Future<StackData>> results = new ArrayList<>(paths.size());
            for (XreStackPath path : paths) {
                results.add(loadersPool.submit(() -> {
                    StackData data = new StackData(path.getPath(), loadHostsForPath.apply(path));
                    log.info("found {} hosts for {}", data.getHosts().map(Collection::size).orElse(0), path.getPath());
                    return data;
                }));
            }

            return results;
        }

        private Set<StackData> merge(Collection<Future<StackData>> futures) {
            Set<StackData> results = new HashSet<>(futures.size());
            for (Future<StackData> future : futures) {
                try {
                    results.add(future.get());
                } catch (ExecutionException | InterruptedException e) {
                    log.error("failed to get hosts ", e);
                }
            }

            return results;
        }
    }
}
