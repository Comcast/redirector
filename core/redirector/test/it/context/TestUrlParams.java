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

import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.common.serializers.JAXBContextBuilder;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.XMLSerializer;

public class TestUrlParams extends TestModelWrapper<UrlRule> {
    String protocol;
    String port;
    String ipv;
    String urn;

    @Override
    public UrlRule value() {
        UrlRule rule = new UrlRule();
        if (port != null) {
            rule.setPort(port);
        }
        rule.setProtocol(protocol);
        if (ipv != null) {
            rule.setIpProtocolVersion(ipv);
        }
        rule.setUrn(urn);

        return rule;
    }

    @Override
    Serializer serializer() {
        return new XMLSerializer(new JAXBContextBuilder().createContextForXML());
    }
}
