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

package com.comcast.tvx.cloud;

import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import org.apache.curator.utils.ZKPaths;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceInstanceBuilder;
import org.apache.curator.x.discovery.ServiceType;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TestServiceUtil {
    private static final Logger log = LoggerFactory.getLogger(TestServiceUtil.class);

    private IDataSourceConnector connector;
    private String registrationPath;

    public TestServiceUtil(IDataSourceConnector connector, String registrationPath) {
        this.connector = connector;
        this.registrationPath = registrationPath;
    }

    public void registerServiceInstance(String region,
                                        String availabilityZone,
                                        String flavor,
                                        String serviceName,
                                        int servicePort,
                                        String serviceAddress,
                                        String serviceAddressV6,
                                        String weight) throws Exception {
        String regPath = new StringBuilder().append(registrationPath).append("/").append(region).append("/")
                .append(availabilityZone).append("/").append(flavor).toString();

        ServiceInstance<MetaData> service = getServiceInstance(serviceName, servicePort, serviceAddress, new HashMap<String, String>() {{
            put("ipv4Address", serviceAddress);
            put("ipv6Address", serviceAddressV6);
            if (weight != null) {
                put("weight", weight);
            }
        }});

        try {
            JsonInstanceSerializer<MetaData> serializer = new JsonInstanceSerializer<>(MetaData.class);
            byte[] bytes = serializer.serialize(service);
            String path = ZKPaths.makePath(ZKPaths.makePath(regPath, service.getName()), service.getId());

            if ( ! connector.isPathExists(path)) {
                connector.createEphemeral(path);
                connector.save(new String(bytes), path);
            } else {
                log.warn("Node " + path + " already exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deRegisterServiceInstance(String region,
                                        String availabilityZone,
                                        String flavor,
                                        String serviceName,
                                        int servicePort, String serviceAddress) throws Exception {
        String regPath = new StringBuilder().append(registrationPath).append("/").append(region).append("/")
            .append(availabilityZone).append("/").append(flavor).toString();

        ServiceInstance<MetaData> service = getServiceInstance(serviceName, servicePort, serviceAddress, Collections.emptyMap());
        String path = ZKPaths.makePath(ZKPaths.makePath(regPath, service.getName()), service.getId());

        if (connector.isPathExists(path)) {
            log.info("Deleting key={}", path);
            connector.delete(path);
        }
    }

    private static ServiceInstance<MetaData> getServiceInstance(
        String serviceName,
        int servicePort,
        String serviceAddress,
        Map<String, String> parameters) throws Exception {

        ServiceInstanceBuilder<MetaData> builder = ServiceInstance.builder();

        // Address is optional.  The Curator library will automatically use the IP from the first
        // ethernet device
        String registerAddress = (serviceAddress == null) ? builder.build().getAddress() : serviceAddress;

        MetaData metadata = new MetaData(UUID.randomUUID(), registerAddress, servicePort, serviceName);
        metadata.setParameters(parameters);

        builder.name(serviceName).payload(metadata).id(registerAddress + ":" +
            String.valueOf(servicePort)).serviceType(ServiceType.DYNAMIC).address(registerAddress).port(servicePort);

        return builder.build();
    }
}
