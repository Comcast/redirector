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

package com.comcast.redirector.dataaccess.cache;

import com.comcast.redirector.dataaccess.client.IDataSourceConnector;

class BaseCacheWrapper {
    protected IDataSourceConnector connector;
    protected boolean useCache = true; //TODO: rename to useCacheWhenConnectedToDataSource
    protected boolean useCacheWhenNotConnectedToDataSource = false;
    private boolean isCacheValid = false;

    BaseCacheWrapper(IDataSourceConnector connector) {
        this.connector = connector;
    }

    final boolean isCacheUsageAllowed() {
        return isCacheValid && (useCache || (useCacheWhenNotConnectedToDataSource && !connector.isConnected()));
    }

    final void allowUseCache() {
        isCacheValid = true;
    }
}
