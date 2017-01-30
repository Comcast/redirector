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

package it.helper;

import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;

import java.util.Collection;

public interface ModelCache {

    Whitelisted getWhitelist();

    Collection<IfExpression> getFlavorRules();

    Collection<IfExpression> getUrlRules();

    Distribution getDistribution();

    Server getDefaultServer();

    UrlRule getUrlParams();

    default void preCacheModel() {
        getFlavorRules();
        getDefaultServer();
        getDistribution();
        getWhitelist();
        getUrlRules();
        getUrlParams();
    }
}
