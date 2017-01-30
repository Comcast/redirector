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

import com.comcast.redirector.api.model.AppNames;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.xrestack.*;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.util.StacksHelper;
import com.comcast.redirector.dataaccess.client.RedirectorDataSourceException;
import com.comcast.redirector.dataaccess.client.RedirectorNoNodeInPathException;
import com.comcast.redirector.dataaccess.dao.IStacksDAO;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.comcast.redirector.common.RedirectorConstants.DELIMETER;

@Service
public class StacksService implements IStacksService {
    private static Logger log = LoggerFactory.getLogger(StacksService.class);
    private final static long  INITIAL_DELAY = 2000; // wait time for first action
    private final static long  REFRESH_DELAY = 24 * 60 * 60 * 1000; // time between the one execution and the next

    private long initialDelay = 0;
    private long refreshDelay = 0;

    @Autowired
    private IAppsService appsService;

    @Autowired
    private IStacksDAO stacksDAO;

    @Autowired
    @Qualifier("whiteListService")
    private IWhiteListService whiteListService;

    @PostConstruct
    public void init() {
        Worker worker = new StacksWorker();
        worker.scheduleRefresh(initialDelay > 0 ? initialDelay : INITIAL_DELAY,
                               refreshDelay > 0 ? refreshDelay : REFRESH_DELAY);
    }

    @Override
    public ServicePaths getStacksForService(String serviceName) {
        Paths stacksWithNodesCount = getPathsForService(serviceName);
        return new ServicePaths(Collections.singletonList(stacksWithNodesCount));
    }

    @Override
    public ServicePaths updateStacksForService(String serviceName, ServicePaths servicePaths, Whitelisted whitelisted) {
        if (whitelisted == null) {
            return servicePaths;
        }

        Set<String> whitelistedSet = StacksHelper.getWhitelistSet(whitelisted);
        Map<String, Integer> flavorsCount = new HashMap<String, Integer>();

        for (PathItem stack : servicePaths.getPaths(serviceName).getStacks()) {
            String fullPath = stack.getValue();
            String stackPath = StacksHelper.getStackPath(fullPath);
            String flavorPath = StacksHelper.getFlavorPath(fullPath);

            if (!flavorsCount.containsKey(flavorPath)) {
                flavorsCount.put(flavorPath, 0);
            }

            if (whitelistedSet.contains(stackPath)) {
                stack.setWhitelistedNodesCount(1);
                int flavorCount = flavorsCount.get(flavorPath);
                flavorsCount.put(flavorPath, ++flavorCount);
            }
            else {
                stack.setWhitelistedNodesCount(0);
            }
        }

        for (PathItem flavor : servicePaths.getPaths(serviceName).getFlavors()) {
            flavor.setWhitelistedNodesCount(flavorsCount.get(flavor.getValue()));
        }
        return servicePaths;
    }

    @Override
    public Set<String> getAllServiceNames() {
        return stacksDAO.getAllAppNamesRegisteredInStacks();
    }

    @Override
    public Boolean isServiceExists(String serviceName) {
        AppNames appNames = appsService.getAppNames();
        return appNames.getAppNames().contains(serviceName);
    }

    @Override
    public ServicePaths getStacksForAllServices(){
        List<Paths> allPaths = new ArrayList<>();
        for (String serviceName : getAllServiceNames()) {
            allPaths.add(getPathsForService(serviceName));
        }

        return new ServicePaths(allPaths);
    }

    @Override
    public Set<PathItem> getActiveStacksAndFlavors (String serviceName) {
        Paths pathsItem = getPathsForService(serviceName);
        return getActiveStacksAndFlavors(pathsItem);
    }

    public Set<PathItem> getActiveStacksAndFlavors(String serviceName, ServicePaths servicePaths) {
        Paths pathsItem = getPathsForService(serviceName, servicePaths);
        return getActiveStacksAndFlavors(pathsItem);
    }

    private Set<PathItem> getActiveStacksAndFlavors (Paths pathsItem) {
        Set<PathItem> servicePaths = new HashSet<>() ;
        // 1. collect active stacks
        for (PathItem item : pathsItem.getStacks()) {
            if (item.getActiveNodesCount() > 0 ) {
                servicePaths.add(item);
            }
        }
        // 2. collect active flavors
        for (PathItem item : pathsItem.getFlavors()) {
            if (item.getActiveNodesCount() > 0) {
                servicePaths.add(item);
            }
        }

        return servicePaths;
    }

    @Override
    public HostIPsListWrapper getHostsForStackOnlyAndService(String stackName, String serviceName) {
        HostIPsListWrapper result = new HostIPsListWrapper();
        List<String> args = Splitter.on(DELIMETER).trimResults().omitEmptyStrings().splitToList(stackName);
        result.getHostIPsList().addAll(getNodesAddressesFromStackOnly(new XreStackPath(args.get(0), args.get(1), null, serviceName)));
        return result;
    }

    @Override
    public HostIPsListWrapper getRandomHostForStackOnlyAndService(String stackOnlyPath, String serviceName) {
        XreStackPath noFlavorStackPath = getNoFlavorXreStackPath(stackOnlyPath, serviceName);
        List<String> flavors = getFlavorsFromStackOnly(noFlavorStackPath);
        Collections.shuffle(flavors);
        for (String flavorName : flavors) {
            try {
                List<HostIPs> randomIp = getRandomHostForStackAndService(stackOnlyPath + DELIMETER + flavorName, serviceName).getHostIPsList();
                if (randomIp.size() > 0) {
                    HostIPsListWrapper result = new HostIPsListWrapper();
                    result.getHostIPsList().addAll(randomIp);
                    return result;
                }
            } catch (Exception ignore) { // we receive exceptions if there are inactive stacks
                log.info("Exception '{}' was caught and ignored while getting random host by stack name without flavor", ignore.getMessage());
            }
        }
        return new HostIPsListWrapper();
    }

    private XreStackPath getNoFlavorXreStackPath(String stackOnlyPath, String serviceName) {
        List<String> dataCenterAndZone = Splitter.on(DELIMETER).trimResults().omitEmptyStrings().splitToList(stackOnlyPath);
        String dataCenter = dataCenterAndZone.get(0);
        String zone = dataCenterAndZone.get(1);

        return new XreStackPath(dataCenter, zone, null /* no flavor */, serviceName);
    }

    @Override
    public HostIPsListWrapper getHostsForStackAndService(String stackName, String serviceName) {
        HostIPsListWrapper result = new HostIPsListWrapper();
        result.getHostIPsList().addAll(getNodesAddresses(new XreStackPath(stackName, serviceName)));
        return result;
    }

    @Override
    public HostIPsListWrapper getRandomHostForStackAndService(String stackName, String serviceName) {
        HostIPsListWrapper result = new HostIPsListWrapper();
        Random random = new Random();
        XreStackPath xreStackPath = new XreStackPath(stackName, serviceName);
        int hostCount = stacksDAO.getHostsCount(xreStackPath);
        if (hostCount > 0) {
            result.getHostIPsList().add(stacksDAO.getHostByIndex(xreStackPath, random.nextInt(hostCount)));
        }
        return result;
    }

    @Override
    public HostIPsListWrapper getHostsForFlavorAndService(String flavorName, String serviceName) {
        HostIPsListWrapper result = new HostIPsListWrapper();
        for (XreStackPath stack : getStackPathsForServiceAndFlavor(serviceName, flavorName)) {
            result.getHostIPsList().addAll(getNodesAddresses(stack));
        }
        return result;
    }

    @Override
    public HostIPsListWrapper getRandomHostForFlavorAndService(String flavorName, String serviceName) {
        List<XreStackPath> stacks = new ArrayList<>(getStackPathsForServiceAndFlavor(serviceName, flavorName));
        Collections.shuffle(stacks);
        HostIPsListWrapper result = new HostIPsListWrapper();
        for (XreStackPath xreStackPath : stacks) {
            result = getRandomHostForStackAndService(xreStackPath.getStackAndFlavorPath(), serviceName);
            if (result.getHostIPsList().size() > 0) {
                return result;
            }
        }
        return result;
    }

    @Override
    public Set<StackData> getAllStacksAndHosts(String serviceName) {
        return FluentIterable
                .from(getStackPathsForService(serviceName))
                .transform(new Function<XreStackPath, StackData>() {
                    @Override
                    public StackData apply(XreStackPath input) {
                        try {
                            return new StackData(input.getPath(), getNodesAddresses(input));
                        } catch (RedirectorDataSourceException e) {
                            throw new ServiceException("failed to get hosts for " + input.getPath(), e);
                        }
                    }
                })
                .toSet();
    }

    public Boolean isStackNameWithoutFlavor (String stackName) {
        return stackName.split(RedirectorConstants.DELIMETER).length == 3;
    }

    @Override
    public HostIPsListWrapper getAddressByStackOrFlavor(String appName, String stackName, String flavorName) {
        HostIPsListWrapper addresses;
        if (StringUtils.isNotBlank(stackName)) {
            addresses = (isStackNameWithoutFlavor(stackName))
                    ? getHostsForStackOnlyAndService(stackName, appName)
                    : getHostsForStackAndService(stackName, appName);
        } else {
            addresses = getHostsForFlavorAndService(flavorName, appName);
        }
        return addresses;
    }

    @Override
    public HostIPsListWrapper getRandomAddressByStackOrFlavor(String serviceName, String stackName, String flavorName) {
        HostIPsListWrapper addresses;
        if (StringUtils.isNotBlank(stackName)) {
            addresses = (isStackNameWithoutFlavor(stackName))
                    ? getRandomHostForStackOnlyAndService(stackName, serviceName)
                    : getRandomHostForStackAndService(stackName, serviceName);
        } else {
            addresses = getRandomHostForFlavorAndService(flavorName, serviceName);
        }

        return addresses;
    }

    @Override
    public void deleteStack(String serviceName, String dataCenter, String availabilityZone, String flavor) {
        log.info("Deleting path: for service {}, dataCenter {}, availabilityZone {}, flavor {}",
                serviceName, dataCenter, availabilityZone, flavor);

        stacksDAO.deleteStackPath(new XreStackPath(dataCenter, availabilityZone, flavor, serviceName));
    }

    @Override
    public synchronized void deleteStacks(Paths paths){
        StringBuffer logMsg = new StringBuffer();
        logMsg.append("Deleting ").append(paths.getStacks().size()).append(" path(s) for ").append(paths.getServiceName()).append(" application: ");
        List<String> nonExistingPaths = new ArrayList<>();
        for (PathItem item : paths.getStacks()) {
            try {
                stacksDAO.deleteStackPath(new XreStackPath(item.getValue(), paths.getServiceName()));
                logMsg.append(item.getValue()).append("; ");
            } catch (RedirectorNoNodeInPathException ex) {
                nonExistingPaths.add(item.getValue());
            }
        }
        log.info(logMsg.toString());
        if (!nonExistingPaths.isEmpty()) {
            throw new WebApplicationException(getMessageError(nonExistingPaths), Response.Status.BAD_REQUEST);
        }
    }

    private String getMessageError(List<String> nonExistingPaths) {
        return "Stack(s) can't be deleted because path(s): " + Joiner.on(";").join(nonExistingPaths) + " does not exist";
    }

    private Paths getPathsForService(String serviceName, ServicePaths servicePaths) {
        if (servicePaths != null) {
            for (Paths currPaths : servicePaths.getPaths()) {
                if (serviceName.equals(currPaths.getServiceName())) {
                    return currPaths;
                }
            }
        }

        return new Paths(serviceName);
    }

    private Paths getPathsForService(String serviceName) {
        Collection<XreStackPath> paths = getStackPathsForService(serviceName);
        return getPathsForServiceWithNodesCount(serviceName, paths);
    }

    private Paths getPathsForServiceWithNodesCount(String serviceName, Collection<XreStackPath> stackPaths) {
        Paths pathsForService = new Paths(serviceName);

        Map<XreStackPath, Integer> stackToNodesCount = getStackToNodesCount(stackPaths);
        Map<XreStackPath, Integer> whitelistedStackToNodesCount = getWhitelistedStackToNodesCount(stackToNodesCount);
        pathsForService.setStacks(getStacksPathItems(stackToNodesCount, whitelistedStackToNodesCount));
        pathsForService.setFlavors(getFlavorsPathItems(stackToNodesCount, whitelistedStackToNodesCount));

        return pathsForService;
    }

    private Map<XreStackPath, Integer> getStackToNodesCount(Collection<XreStackPath> stackPaths) {
        return Maps.toMap(stackPaths, new Function<XreStackPath, Integer>() {
            @Override
            public Integer apply(XreStackPath input) {
                try {
                    return stacksDAO.getHostsCount(input);
                } catch (RedirectorDataSourceException e) {
                    log.error("failed to get hosts count for " + input.getPath());
                    throw new ServiceException("failed to get hosts count for " + input.getPath(), e);
                }
            }
        });
    }

    private Map<XreStackPath, Integer> getWhitelistedStackToNodesCount(Map<XreStackPath, Integer> stacksNodeCount) {
        return Maps.filterEntries(stacksNodeCount, new Predicate<Map.Entry<XreStackPath, Integer>>() {
            @Override
            public boolean apply(Map.Entry<XreStackPath, Integer> input) {
                XreStackPath path = input.getKey();
                try {
                    return isPathWhitelisted(path);
                } catch (RedirectorDataSourceException e) {
                    log.error("failed to identify if path {} is whitelisted {}", path, e);
                    throw new ServiceException("failed to get hosts count for " + path, e);
                }
            }
        });
    }

    List<PathItem> getStacksPathItems(Map<XreStackPath, Integer> stackToNodesCount, final Map<XreStackPath, Integer> whitelistedStackToNodesCount) {
        return Lists.newArrayList(Collections2.transform(stackToNodesCount.entrySet(), new Function<Map.Entry<XreStackPath, Integer>, PathItem>() {
            @Override
            public PathItem apply(Map.Entry<XreStackPath, Integer> input) {
                XreStackPath stack = input.getKey();
                int count = input.getValue();
                int whitelistedCount = whitelistedStackToNodesCount.containsKey(stack)
                        ? whitelistedStackToNodesCount.get(stack) : 0;

                return new PathItem(stack.getStackAndFlavorPath(), count, whitelistedCount);
            }
        }));
    }

    List<PathItem> getFlavorsPathItems(Map<XreStackPath, Integer> stackToNodesCount, Map<XreStackPath, Integer> whitelistedStackToNodesCount) {
        Map<String, Integer> flavorToCount = getFlavorToCountMap(stackToNodesCount);
        final Map<String, Integer> flavorToWhitelistedCount = getFlavorToCountMap(whitelistedStackToNodesCount);

        return Lists.newArrayList(Collections2.transform(flavorToCount.entrySet(), new Function<Map.Entry<String, Integer>, PathItem>() {
            @Override
            public PathItem apply(Map.Entry<String, Integer> input) {
                String flavor = input.getKey();
                int count = input.getValue();
                int whitelistedCount = flavorToWhitelistedCount.containsKey(flavor) ? flavorToWhitelistedCount.get(flavor) : 0;
                return new PathItem(flavor, count, whitelistedCount);
            }
        }));
    }

    private Map<String, Integer> getFlavorToCountMap(Map<XreStackPath, Integer> stackToNodesCount) {
        Map<String, Integer> flavorToCount = new HashMap<>();
        for (Map.Entry<XreStackPath, Integer> input : stackToNodesCount.entrySet()) {
            XreStackPath stack = input.getKey();
            Integer count = input.getValue();

            if (flavorToCount.containsKey(stack.getFlavor())) {
                flavorToCount.put(stack.getFlavor(), flavorToCount.get(stack.getFlavor()) + count);
            } else {
                flavorToCount.put(stack.getFlavor(), count);
            }
        }

        return flavorToCount;
    }

    private Collection<XreStackPath> getStackPathsForService(final String serviceName) {
        return Collections2.filter(stacksDAO.getAllStackPaths(), new Predicate<XreStackPath>() {
            @Override
            public boolean apply(XreStackPath input) {
                return input.getServiceName().equals(serviceName);
            }
        });
    }

    private Collection<XreStackPath> getStackPathsForServiceAndFlavor(final String serviceName, final String flavor) {
        return Collections2.filter(stacksDAO.getAllStackPaths(), new Predicate<XreStackPath>() {
            @Override
            public boolean apply(XreStackPath input) {
                return input.getServiceName().equals(serviceName) && input.getFlavor().equals(flavor);
            }
        });
    }

    private List<HostIPs> getNodesAddresses (XreStackPath path) {
        return Lists.newArrayList(stacksDAO.getHosts(path));
    }

    private List<HostIPs> getNodesAddressesFromStackOnly (XreStackPath path) {
        return Lists.newArrayList(stacksDAO.getHostsByStackOnlyPath(path));
    }

    private List<String> getFlavorsFromStackOnly (XreStackPath path) {
        return stacksDAO.getFlavorsByStackOnlyPath(path);
    }


    private boolean isPathWhitelisted(XreStackPath path) {
        return whiteListService.getWhitelistedStacks(path.getServiceName()).getPaths().contains(path.getStackOnlyPath());
    }

    // for unit tests
    void setStacksDAO(IStacksDAO stacksDAO) {
        this.stacksDAO = stacksDAO;
    }

    public void setWhiteListService(IWhiteListService whiteListService) {
        this.whiteListService = whiteListService;
    }

    public void setInitialDelay(long initialDelay) {
        this.initialDelay = initialDelay;
    }

    public void setRefreshDelay(long refreshDelay) {
        this.refreshDelay = refreshDelay;
    }

    //
    private interface Worker {
        void scheduleRefresh(long initialDelay, long refreshDelay);
    }

    private class StacksWorker implements Runnable, Worker {
        private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        private ScheduledFuture scheduled;

        @Override
        public void run() {
            log.info("StackWorker was started, deleting Stacks without hosts");
            Set<String> allServiceNames = getAllServiceNames();
            for (String serviceNames : allServiceNames) {
                Paths pathsForService = getPathsForService(serviceNames);
                deleteStacks(pathsForService);
            }
            log.info("StackWorker was finished");
        }

        @Override
        public void scheduleRefresh(long initialDelay, long refreshDelay) {
            if (scheduled != null) {
                scheduled.cancel(false);
            }

            this.scheduled = executorService.scheduleWithFixedDelay(this,
                    initialDelay,
                    refreshDelay,
                    TimeUnit.MILLISECONDS);
        }
    }
}
