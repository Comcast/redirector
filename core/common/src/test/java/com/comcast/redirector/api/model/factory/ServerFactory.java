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

package com.comcast.redirector.api.model.factory;

import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.builders.ServerBuilder;

public class ServerFactory {

    public static Server newSimpleServerForFlavor(final String path) {
        return new ServerBuilder().withName("0").withFlavor(path).build();
    }

    @Deprecated // Use ServerBuilder instead
    public static Server newServerSimpleWithName(final String path, final String name) {
        return new ServerBuilder().withName(name).withFlavor(path).build();
    }

    public static Server newServerAdvanced(final String url) {
        return new ServerBuilder().withName("0").withFlavor("").withUrl(url).build();
    }

    @Deprecated // Use ServerBuilder instead
    public static Server newServerSimple(final String path, final String url) {
        return new ServerBuilder().withFlavor(path).withUrl(url).build();
    }

    @Deprecated // Use ServerBuilder instead
    public static Server newServerSimple(final String path, final String name, final String url,
            final String description, final String isNonWhitelisted) {

        return new ServerBuilder().withFlavor(path).withName(name).withDescription(description)
            .withUrl(url).withIsNonWhitelisted(isNonWhitelisted)
            .build();
    }
}
