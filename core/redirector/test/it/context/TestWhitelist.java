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

package it.context;

import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.common.serializers.JAXBContextBuilder;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.XMLSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TestWhitelist extends TestModelWrapper<Whitelisted> {
    private Set<String> stacks;

    public TestWhitelist(String ... stacks) {
        this.stacks = new HashSet<>(Arrays.asList(stacks));
    }

    @Override
    public Whitelisted value() {
        Whitelisted whitelisted = new Whitelisted();
        whitelisted.setPaths(new ArrayList<>(stacks));

        return whitelisted;
    }

    @Override
    Serializer serializer() {
        return new XMLSerializer(new JAXBContextBuilder().createContextForXML());
    }
}
