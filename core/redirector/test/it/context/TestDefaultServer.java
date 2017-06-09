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

import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.common.serializers.JAXBContextBuilder;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.XMLSerializer;

public class TestDefaultServer extends TestModelWrapper<Server> {
    String flavor;
    String advancedUrl;

    @Override
    public Server value() {
        Server server = new Server();
        server.setUrl(RedirectorConstants.URL_TEMPLATE);
        if (advancedUrl != null) {
            server.setUrl(advancedUrl);
        } else {
            server.setPath(flavor);
        }

        return server;
    }

    public String getFlavor() {
        return flavor;
    }

    @Override
    Serializer serializer() {
        return new XMLSerializer(new JAXBContextBuilder().createContextForXML());
    }
}
