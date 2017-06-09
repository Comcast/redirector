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
 * @author Roman Dolomansky (rdolomansky@productengine.com)
 */

package com.comcast.redirector.api.model.builders;

import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.common.RedirectorConstants;

public class ServerBuilder {
    private String name = RedirectorConstants.DEFAULT_SERVER_NAME;
    private String flavor;
    private String url = RedirectorConstants.URL_TEMPLATE;
    private String description = name + " server route";
    private String isNonWhitelisted = "false";

    public ServerBuilder withFlavor(String flavor) {
        this.flavor = flavor;
        return this;
    }

    public ServerBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ServerBuilder withUrl(String url) {
        this.url = url;
        return this;
    }

    public ServerBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public ServerBuilder withIsNonWhitelisted(String isNonWhitelisted) {
        this.isNonWhitelisted = isNonWhitelisted;
        return this;
    }

    public Server build() {
        Server server = new Server();
        server.setPath(flavor);
        server.setName(name);
        server.setUrl(url);
        server.setDescription(description);
        server.setIsNonWhitelisted(isNonWhitelisted);

        return server;
    }
}
