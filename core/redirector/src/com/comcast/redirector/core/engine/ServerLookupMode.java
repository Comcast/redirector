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

package com.comcast.redirector.core.engine;

public enum ServerLookupMode {
    DEFAULT(FilterMode.WHITELIST_ONLY, false),
    DEFAULT_NON_WHITELISTED(FilterMode.NON_WHITELIST, false),
    NON_FILTERED(FilterMode.NO_FILTER, false),
    NON_FILTERED_BACKUP(FilterMode.NO_FILTER, true);

    private FilterMode filter;
    private boolean forceGetFromBackup;

    private ServerLookupMode(FilterMode filter, boolean forceGetFromBackup) {
        this.filter = filter;
        this.forceGetFromBackup = forceGetFromBackup;
    }

    public FilterMode getFilterMode() {
        return filter;
    }

    public boolean isForceGetFromBackup() {
        return forceGetFromBackup;
    }


    @Override
    public String toString() {
        return new StringBuilder("[ applyFilter: ")
                        .append(filter.name())
                        .append(", forceGetFromBackup: ")
                        .append(forceGetFromBackup)
                        .append("]")
                        .toString();
    }
}
