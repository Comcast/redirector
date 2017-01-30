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

package it.context;

import java.util.function.Supplier;

public interface ContextBuilderMain {
    Supplier<ContextBuilderMain> builderMainSupplier();

    default ContextFlavorRuleBuilder withFlavorRule() {
        return builderMainSupplier().get().withFlavorRule();
    }

    default ContextURLRuleBuilder withUrlRule() {
        return builderMainSupplier().get().withUrlRule();
    }

    default DistributionBuilder withDistribution() {
        return builderMainSupplier().get().withDistribution();
    }

    default DefaultServerBuilder withDefaultServer() {
        return builderMainSupplier().get().withDefaultServer();
    }

    default DefaultUrlParamsBuilder withDefaultUrlParams() {
        return builderMainSupplier().get().withDefaultUrlParams();
    }

    default EmptyStacksBuilder withEmptyStacks() {
        return builderMainSupplier().get().withEmptyStacks();
    }

    default StacksWithDataBuilder withStacksWithData() {
        return builderMainSupplier().get().withStacksWithData();
    }

    default RedirectorInstancesBuilder withRedirectorInstances() {
        return builderMainSupplier().get().withRedirectorInstances();
    }

    default ContextBuilderMain withWhitelist(String... stacks) {
        return builderMainSupplier().get().withWhitelist(stacks);
    }

    default ContextBuilderMain withApplications(String... applications) {
        return builderMainSupplier().get().withApplications(applications);
    }

    default ContextBuilderMain withNamespacedList(String name, String... values) {
        return builderMainSupplier().get().withNamespacedList(name, values);
    }

    default HostsBuilder withHosts() {
        return builderMainSupplier().get().withHosts();
    }

    default ContextBuilderMain withVersion(int version) {
        return builderMainSupplier().get().withVersion(version);
    }

    default TestContext build() {
        return builderMainSupplier().get().build();
    }
}
