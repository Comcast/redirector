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

package com.comcast.redirector.core.balancer.serviceprovider.weight;

import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.tvx.cloud.MetaData;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.x.discovery.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstanceWeigher implements IInstanceWeigher<MetaData> {

    private static Logger log = LoggerFactory.getLogger(InstanceWeigher.class);

    private final ZKConfig config;
    public InstanceWeigher(ZKConfig config) {
        this.config = config;
    }

    @Override
    public int getWeight(ServiceInstance<MetaData> instance) {
        int weight = config.getDefaultWeightOfTheNode(); //TODO: Need to update weight after apply changes in UI
        if (instance.getPayload().getParameters() != null) {
            String weightStr = instance.getPayload().getParameters().get("weight");

            if (weightStr != null && StringUtils.isNotBlank(weightStr)) {
                if (config.isWeightFromZookeeperApplied()) {
                    try {
                        weight = Math.round((float) Double.parseDouble(weightStr));

                        String fractionalPart = weightStr.contains(".") ? weightStr.substring(weightStr.lastIndexOf(".") + 1, weightStr.length()) : null;
                        if (StringUtils.isNotBlank(fractionalPart) && Integer.parseInt(fractionalPart) > 0) {
                            log.warn("ServiceInstance (name={} address={}) registered with value of weight equals to {}, its fractional part equals to {} but should be 0. Weight will be rounded to {}.",
                                instance.getName(), instance.getAddress(), weightStr, fractionalPart, weight);
                        }
                    } catch (NumberFormatException e) {
                        log.warn("ServiceInstance (name={} address={}) registered with value of weight equals to {}, which is not a number. Default weight will be applied: {}.",
                            instance.getName(), instance.getAddress(), weightStr, weight);
                    }
                } else {
                    log.info("Host weight={} is ignored and Default weight={} is used since isWeightFromZookeeperApplied = false", weightStr, weight);
                }
            }

            if (weight > config.getMaxWeightOfTheNode()) {
                log.warn("ServiceInstance (name={} address={}) registered with value of weight equals to {}, which exceeds max {}. So weight {} applied for the node", instance.getName(), instance.getAddress(), weightStr, config.getMaxWeightOfTheNode(), config.getMaxWeightOfTheNode());
                weight = config.getMaxWeightOfTheNode();
            }
        }
        return weight;
    }
}
