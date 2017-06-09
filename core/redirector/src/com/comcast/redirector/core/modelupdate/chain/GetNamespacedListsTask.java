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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.redirector.core.modelupdate.chain;

import com.comcast.redirector.common.util.ThreadLocalLogger;
import com.comcast.redirector.core.modelupdate.holder.IModelHolder;
import com.comcast.redirector.core.modelupdate.holder.IDataStoreAwareNamespacedListsHolder;


public class GetNamespacedListsTask implements IProcessTask {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(GetNamespacedListsTask.class);

    private static final String NAMESPACED_LISTS_IS_NOT_VALID = "NamespacedLists is not valid.";

    private boolean fromDataStore;
    private IDataStoreAwareNamespacedListsHolder namespacedListHolder;

    public GetNamespacedListsTask(IDataStoreAwareNamespacedListsHolder namespacedListHolder, boolean fromDataStore) {
        this.fromDataStore = fromDataStore;
        this.namespacedListHolder = namespacedListHolder;
    }

    @Override
    public Result handle(ModelContext context) {
        loadNamespacedLists();
        return Result.success(context);
    }

    private void loadNamespacedLists() {
        if (isEmptyNamespacedLists()) {
            log.info("Namespaced lists are not loaded yet. Loading...");
            namespacedListHolder.load(fromDataStore);
        }

        if (namespacedListHolder.isNamespacedListsOutOfDate()) {
            log.info("Namespaced lists are out of date. Loading from Data Store");
            namespacedListHolder.load(IModelHolder.GET_FROM_DATA_STORE);
        }
    }

    private boolean isEmptyNamespacedLists() {
       return namespacedListHolder.getNamespacedListsBatch().getNamespacedLists().isEmpty();
    }
}
