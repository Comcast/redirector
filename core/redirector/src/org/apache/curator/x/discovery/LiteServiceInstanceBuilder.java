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

package org.apache.curator.x.discovery;

import com.comcast.tvx.cloud.MetaData;
//import org.apache.curator.x.discovery.ServiceInstanceBuilder;

/**
 * Workaround because ServiceInstance.builder calls getAllLocalIPs which in turn calls NetworkInterface.getNetworkInterfaces
 * which leads to pretty huge delay.
 * This class is used only for OfflineServiceProvider.
 *
 * See bug APPDS-655 for more info.
 */
public class LiteServiceInstanceBuilder extends ServiceInstanceBuilder<MetaData> {
    public LiteServiceInstanceBuilder() {

    }
}
