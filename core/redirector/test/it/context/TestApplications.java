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

import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.core.JsonSerializer;
import com.comcast.redirector.core.applications.Applications;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestApplications extends TestModelWrapper<Applications> {
    Set<String> applications;

    public TestApplications(String... apps) {
        applications = Stream.of(apps).collect(Collectors.toSet());
    }

    @Override
    Serializer serializer() {
        return new JsonSerializer();
    }

    @Override
    public Applications value() {
        return new Applications(applications);
    }
}
