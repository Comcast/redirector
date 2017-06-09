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

package com.comcast.redirector.dataaccess.dao;

import com.comcast.redirector.dataaccess.dao.ICacheableDAO;
import com.comcast.redirector.dataaccess.EntityType;

/**
 * DAO Factory that registers DAOs inside itself when creating DAOs.
 * Client of such a factory can get DAO by type from factory registry
 */
public interface IRegisteringDAOFactory {
    ICacheableDAO getRegisteredCacheableDAO(EntityType entityType);
}
