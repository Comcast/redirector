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

package com.comcast.redirector.api.redirector.service.redirectortestsuite;

import com.comcast.redirector.api.redirector.utils.CoreUtils;
import com.comcast.redirector.common.serializers.JAXBContextBuilder;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.XMLSerializer;
import com.comcast.redirector.core.engine.IRedirectorEngineFactory;
import com.comcast.redirector.core.engine.RedirectorEngineFactory;
import com.comcast.xre.common.redirector.selector.xmlimpl.Config;

public class RedirectorFactory {
    public static IRedirectorEngineFactory getRedirectorEngineFactory() {
        RedirectorEngineFactory result = new RedirectorEngineFactory(CoreUtils.newServiceProviderManagerFactory());
        result.setConfig(new Config());
        result.setSerializer(newSerializer());
        result.setIsStaticDiscoveryNeededForApp(app -> true);

        return result;
    }

    private static Serializer newSerializer() {
        return new XMLSerializer(new JAXBContextBuilder().createContextForXML());
    }
}
