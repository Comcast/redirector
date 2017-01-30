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

package com.comcast.redirector.dataaccess.cache.factory;

import com.comcast.redirector.dataaccess.cache.IPathChildrenCacheWrapper;

import java.util.concurrent.ThreadFactory;

public interface IPathChildrenCacheFactory {
    IPathChildrenCacheWrapper newPathChildrenCacheWrapper(String path, boolean useCache);
    IPathChildrenCacheWrapper newPathChildrenCacheWrapper(String path, boolean dataIsCompressed, ThreadFactory threadFactory, boolean useCache);
}
