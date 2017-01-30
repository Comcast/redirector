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

package com.comcast.redirector.api.redirector.service.redirectortestsuite;

import com.comcast.redirector.api.model.namespaced.Namespaces;
import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.url.rule.Default;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.xrestack.HostIPsListWrapper;
import com.comcast.redirector.api.model.xrestack.PathItem;
import com.comcast.redirector.api.model.xrestack.ServicePaths;
import com.comcast.redirector.api.model.xrestack.StackData;
import com.comcast.redirector.common.util.RulesUtils;
import com.comcast.redirector.ruleengine.repository.NamespacedListsBatch;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

class ExternalEndpointRedirectorEnvLoader implements RedirectorTestSuiteService.IRedirectorEnvLoader {
    private static final Logger log = LoggerFactory.getLogger(ExternalEndpointRedirectorEnvLoader.class);
    private String serviceName;

    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private static Client client = ClientBuilder.newClient();
    private WebTarget basePath;

    private static final String SERVICE_PLACEHOLDER = "{serviceName}";

    private enum EntityType {
        FLAVOR_RULES(SelectServer.class, new String[] {"rules", SERVICE_PLACEHOLDER}),
        DISTRIBUTION(Distribution.class, new String[] {"distributions", SERVICE_PLACEHOLDER}),
        DEFAULT_SERVER(Server.class, new String[] {"servers", SERVICE_PLACEHOLDER, "default"}),
        URL_RULES(URLRules.class, new String[] {"urlRules", SERVICE_PLACEHOLDER}),
        DEFAULT_URL_PARAMS(Default.class, new String[] {"urlRules", SERVICE_PLACEHOLDER, "defaultUrlParams"}),
        NAMESPACED_LISTS(Namespaces.class, new String[] {"namespacedLists"}),
        WHITELIST(Whitelisted.class, new String[] {"whitelist", SERVICE_PLACEHOLDER}),
        STACKS(ServicePaths.class, new String[] {"stacks", SERVICE_PLACEHOLDER}),
        HOSTS(HostIPsListWrapper.class, new String[] {"stacks", SERVICE_PLACEHOLDER, "addresses"});

        private Class<?> entityClass;
        private String[] entityPathComponents;

        EntityType(Class<?> entityClass, String[] entityPathComponents) {
            this.entityClass = entityClass;
            this.entityPathComponents = entityPathComponents;
        }
    }

    public ExternalEndpointRedirectorEnvLoader(String serviceName, String baseURL) {
        this.serviceName = serviceName;
        basePath = client.target(baseURL);
    }

    private WebTarget getWebTarget(String[] pathItem, KeyValue[] queryParams) {
        WebTarget result = basePath;
        for (String item : pathItem) {
            result = result.path(item.replace(SERVICE_PLACEHOLDER, serviceName));
        }

        for (KeyValue param : queryParams) {
            result = result.queryParam(param.key, param.value);
        }

        return result;
    }

    private Invocation.Builder getRequestBuilder(String[] pathItem, KeyValue[] queryParams) {
        return getWebTarget(pathItem, queryParams).request().accept(MediaType.APPLICATION_JSON);
    }

    private <T> T executeGetRequest(Class<T> modelClass, String[] pathItem, KeyValue... queryParams) {
        try {
        Response response = getRequestBuilder(pathItem, queryParams).get();
        return response.readEntity(modelClass);
        } catch (Exception ex) {
            String messageError = "Failed to get test cases";
            log.error(ex.getMessage(), ex);
            throw new WebApplicationException(messageError, Response.Status.BAD_REQUEST);
        }
    }

    @SuppressWarnings({"unchecked"})
    private <T> T readEntity(EntityType entityType, KeyValue... queryParams)  {
        return (T)executeGetRequest(entityType.entityClass, entityType.entityPathComponents, queryParams);
    }

    @Override
    public SelectServer getFlavorRules() {
        SelectServer selectServer = readEntity(EntityType.FLAVOR_RULES);
        Distribution distribution = readEntity(EntityType.DISTRIBUTION);
        Server defaultServer = readEntity(EntityType.DEFAULT_SERVER);

        if (defaultServer != null && distribution != null) {
            distribution.setDefaultServer(defaultServer);
        }
        return RulesUtils.buildSelectServer(selectServer.getItems(), distribution);
    }

    @Override
    public URLRules getUrlRules() {
        URLRules urlRules = readEntity(EntityType.URL_RULES);
        Default defaultUrlParams = readEntity(EntityType.DEFAULT_URL_PARAMS);

        return RulesUtils.buildURLRules(urlRules.getItems(), defaultUrlParams);
    }

    @Override
    public NamespacedListsBatch getNamespacedListsBatch() {
        Namespaces namespaces = readEntity(EntityType.NAMESPACED_LISTS);
        return RedirectorTestSuiteService.getNamespacedListsBatch(namespaces);
    }

    @Override
    public Whitelisted getWhitelists() {
        return readEntity(EntityType.WHITELIST);
    }

    @Override
    public Set<StackData> getStacks() {
        Set<StackData> result = Collections.emptySet();

        ServicePaths paths = readEntity(EntityType.STACKS);
        if (paths.getPaths().size() == 0) {
            return result;
        }

        List<Callable<StackWithHosts>> jobsForHosts = FluentIterable
            .from(paths.getPaths().get(0).getStacks())
            .filter(new Predicate<PathItem>() {
                @Override
                public boolean apply(PathItem input) {
                    return input.getActiveNodesCount() > 0;
                }
            })
            .transform(new Function<PathItem, Callable<StackWithHosts>>() {
                @Override
                public Callable<StackWithHosts> apply(final PathItem input) {
                    return new Callable<StackWithHosts>() {
                        @Override
                        public StackWithHosts call() throws Exception {
                            HostIPsListWrapper hosts = readEntity(EntityType.HOSTS, new KeyValue("stackName", input.getValue()));
                            return new StackWithHosts(input.getValue(), hosts);
                        }
                    };
                }
            }).toList();

        try {
            List<Future<StackWithHosts>> results = executorService.invokeAll(jobsForHosts);
            return FluentIterable
                .from(results)
                .transform(new Function<Future<StackWithHosts>, StackData>() {
                    @Override
                    public StackData apply(Future<StackWithHosts> input) {
                        try {
                            StackWithHosts t = input.get();
                            return new StackData(t.stack + "/" + serviceName, t.hosts.getHostIPsList());
                        } catch (InterruptedException | ExecutionException e) {
                            log.error("Can't get hosts for stacks", e);
                        }
                        return null;
                    }
                }).filter(Predicates.notNull()).toSet();
        } catch (InterruptedException e) {
            log.error("Can't get hosts for stacks", e);
        }

        return result;
    }

    private class StackWithHosts {
        final String stack;
        final HostIPsListWrapper hosts;

        public StackWithHosts(String stack, HostIPsListWrapper hosts) {
            this.stack = stack;
            this.hosts = hosts;
        }
    }

    private class KeyValue {
        final String key;
        final String value;

        private KeyValue(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}
