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

package com.comcast.redirector.core.balancer.serviceprovider;

import com.comcast.redirector.api.model.xrestack.HostIPs;
import com.comcast.redirector.api.model.xrestack.StackData;
import com.comcast.redirector.api.model.xrestack.XreStackPath;
import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.tvx.cloud.MetaData;
import org.apache.curator.x.discovery.ProviderStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

abstract class AbstractServiceProviderManager implements IServiceProviderManager {
    private static final Logger log = LoggerFactory.getLogger(AbstractServiceProviderManager.class);

    protected final ProviderStrategy<MetaData> providerStrategy; // TODO: make ZKConfig return providerStrategy
    protected final ZKConfig config;
    private final Set<String> excludedApps;


    private DynamicProvidersUpdateService providersUpdateService;

    // TODO: think if we need these vars to be volatile
    private volatile Map<StackData, IFilteredServiceProvider> stackServiceProviders = Collections.emptyMap();
    private volatile Map<FlavorAndAppTuple, IAggregateServiceProvider> flavorAndAppServiceProviders = Collections.emptyMap();

    AbstractServiceProviderManager(ProviderStrategy<MetaData> providerStrategy, ZKConfig config) {
        this.providerStrategy = providerStrategy;
        this.config = config;
        this.excludedApps = config.getExcludedAppsFromStackAutoDiscovery();
    }

    @Override
    public List<IFilteredServiceProvider> getAllStackServiceProviders() {
        return stackServiceProviders.values().stream().collect(toList());
    }

    @Override
    public IFilteredServiceProvider getStackServiceProvider(StackData stackData) {
        return stackServiceProviders.get(stackData);
    }

    @Override
    public IFilteredServiceProvider getServiceProviderForFlavorAndApp(String flavor, String appName) {
        return flavorAndAppServiceProviders.get(new FlavorAndAppTuple(flavor, appName));
    }

    protected final void switchToDynamicMode(DynamicServiceProviderFactory serviceProviderFactory) {
        providersUpdateService = new DynamicProvidersUpdateService(serviceProviderFactory, excludedApps);
    }

    protected final void applyStacksChanges(Set<XreStackPath> addedStacks,
                                            Set<XreStackPath> removedStacks) {
        ServiceProviders providers = providersUpdateService.update(getAllStackServiceProviders(), addedStacks, removedStacks);

        applyServiceProviders(providers);
    }

    protected final void applyServiceProviders(ServiceProviders serviceProviders) {
        stackServiceProviders = serviceProviders.getProvidersForStacks().stream()
            .collect(
                toMap(
                    provider -> new StackData(provider.getStack().getPath()),
                    Function.identity()));

        flavorAndAppServiceProviders = serviceProviders.getProvidersPerFlavorAndApp();
    }

    protected final void reset() {
        stackServiceProviders.values().forEach(provider -> {
            try {
                provider.close();
            } catch (IOException e) {
                log.error("Failed to close provider for path=" + provider.getStack().getPath(), e);
            }
        });
        stackServiceProviders.clear();
        flavorAndAppServiceProviders.clear();
    }

    private class DynamicProvidersUpdateService {

        private DynamicServiceProviderFactory serviceProviderFactory;
        private Set<String> excludedApps;

        DynamicProvidersUpdateService(DynamicServiceProviderFactory serviceProviderFactory, Set<String> excludedApps) {
            this.serviceProviderFactory = serviceProviderFactory;
            this.excludedApps = excludedApps;
        }

        ServiceProviders update(Collection<IFilteredServiceProvider> currentProviders,
                                Set<XreStackPath> addedStacks,
                                Set<XreStackPath> deletedStacks) {
            // Closing providers for deleted stacks
            Set<String> deletedStackPaths = deletedStacks.stream()
                .filter(this::isStackForEligibleApp)
                .map(XreStackPath::getPath).collect(toSet());
            Predicate<IFilteredServiceProvider> toBeClosed = provider -> deletedStackPaths.contains(provider.getStack().getPath());
            currentProviders.stream()
                .filter(toBeClosed)
                .forEach(provider -> {
                    try {
                        provider.close();
                    } catch (IOException e) {
                        log.error("Failed to close provider for path=" + provider.getStack().getPath(), e);
                    }
                });

            // Creating providers for added stacks and remaining stacks
            Set<IFilteredServiceProvider> updatedStackProviders = currentProviders.stream()
                .filter(toBeClosed.negate())
                .collect(Collectors.toCollection((Supplier<Set<IFilteredServiceProvider>>) HashSet::new));

            Set<String> updatedServiceProviderPaths = updatedStackProviders.stream()
                .map(provider -> provider.getStack().getPath()).collect(toSet());
            addedStacks.stream()
                .filter(this::isStackForEligibleApp)
                .filter(stack -> ! updatedServiceProviderPaths.contains(stack.getPath()))
                .map(this::createServiceProvider)
                .forEach(updatedStackProviders::add);

            List<IFilteredServiceProvider> providersForStacks = updatedStackProviders.stream().collect(toList());
            Map<FlavorAndAppTuple, IAggregateServiceProvider> providersForFlavors =
                new StackToFlavorProvidersTranslationService().apply(providersForStacks);

            return new ServiceProviders(providersForStacks, providersForFlavors);
        }

        private boolean isStackForEligibleApp(XreStackPath stack) {
            return ! config.getExcludedAppsFromStackAutoDiscovery().contains(stack.getServiceName());
        }

        private IFilteredServiceProvider createServiceProvider(XreStackPath stackData) {
            return serviceProviderFactory.createServiceProvider(stackData);
        }
    }

    class StaticProvidersUpdateService implements Function<Set<StackData>, ServiceProviders> {

        private IStaticServiceProviderFactory serviceProviderFactory;
        private Set<String> excludedApps = Collections.emptySet();

        StaticProvidersUpdateService(IStaticServiceProviderFactory serviceProviderFactory) {
            this.serviceProviderFactory = serviceProviderFactory;
        }

        StaticProvidersUpdateService(IStaticServiceProviderFactory serviceProviderFactory, Set<String> excludedApps) {
            this(serviceProviderFactory);
            this.excludedApps = excludedApps;
        }

        @Override
        public ServiceProviders apply(Set<StackData> stacks) {
            List<IFilteredServiceProvider> providersForStacks = stacks.stream()
                .filter(this::isStackForEligibleApp)
                .map(this::createServiceProviderFromStackWithHosts).collect(toList());

            Map<FlavorAndAppTuple, IAggregateServiceProvider> providersPerFlavorAndApp =
                new StackToFlavorProvidersTranslationService().apply(providersForStacks);

            return new ServiceProviders(providersForStacks, providersPerFlavorAndApp);
        }

        private boolean isStackForEligibleApp(StackData stack) {
            return ! excludedApps.contains(stack.getServiceName());
        }

        private IFilteredServiceProvider createServiceProviderFromStackWithHosts(StackData stackData) {
            List<HostIPs> hosts = stackData.getHosts().orElse(new ArrayList<>());
            XreStackPath stackPath = new XreStackPath(stackData.getPath());

            return serviceProviderFactory.createServiceProvider(stackPath, hosts);
        }
    }

    private class StackToFlavorProvidersTranslationService
        implements Function<List<IFilteredServiceProvider>, Map<FlavorAndAppTuple, IAggregateServiceProvider>> {

        @Override
        public Map<FlavorAndAppTuple, IAggregateServiceProvider> apply(List<IFilteredServiceProvider> providersForStacks) {
            Map<FlavorAndAppTuple, List<IFilteredServiceProvider>> providersGroupedByFlavor = providersForStacks.stream()
                .collect(groupingBy(this::getFlavorAndApp));

            return providersGroupedByFlavor.entrySet().stream()
                .collect(
                    toMap(
                        Map.Entry::getKey,
                        entry -> createAggregateServiceProvider(entry.getKey().getFlavor(), entry.getValue())
                    )
                );
        }

        private FlavorAndAppTuple getFlavorAndApp(IFilteredServiceProvider provider) {
            return new FlavorAndAppTuple(provider.getStack().getFlavor(), provider.getStack().getServiceName());
        }

        private IAggregateServiceProvider createAggregateServiceProvider(String flavor, List<IFilteredServiceProvider> providers) {
            shuffleProviders(providers);
            return new AggregateServiceProvider(providerStrategy, providers, flavor);
        }

        private void shuffleProviders(List<IFilteredServiceProvider> providers) {
            Collections.shuffle(providers, new Random(System.nanoTime()));
        }
    }

    private static class FlavorAndAppTuple {
        String flavor;
        String appName;

        FlavorAndAppTuple(String flavor, String appName) {
            this.flavor = flavor;
            this.appName = appName;
        }

        public String getFlavor() {
            return flavor;
        }

        public String getAppName() {
            return appName;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("FlavorAndAppTuple{");
            sb.append("flavor='").append(flavor).append('\'');
            sb.append(", appName='").append(appName).append('\'');
            sb.append('}');
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FlavorAndAppTuple that = (FlavorAndAppTuple) o;
            return Objects.equals(flavor, that.flavor) &&
                Objects.equals(appName, that.appName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(flavor, appName);
        }
    }

    static class ServiceProviders {
        private List<IFilteredServiceProvider> providersForStacks;
        private Map<FlavorAndAppTuple, IAggregateServiceProvider> providersForFlavors;

        ServiceProviders(List<IFilteredServiceProvider> providersForStacks,
                         Map<FlavorAndAppTuple, IAggregateServiceProvider> providersPerFlavorAndApp) {
            this.providersForStacks = providersForStacks;
            this.providersForFlavors = providersPerFlavorAndApp;
        }

        List<IFilteredServiceProvider> getProvidersForStacks() {
            return providersForStacks;
        }

        Map<FlavorAndAppTuple, IAggregateServiceProvider> getProvidersPerFlavorAndApp() {
            return providersForFlavors;
        }
    }
}
