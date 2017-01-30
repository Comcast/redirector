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
 * @author Roman Dolomansky (rdolomansky@productengine.com)
 */

package com.comcast.redirector.endpoint.http.controllers;

import com.comcast.redirector.RedirectorGateway;
import com.comcast.redirector.endpoint.http.model.ServiceInfo;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
public class DataServiceInfoController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataServiceInfoController.class);

    private ServiceInfo serviceInfo = obtainServiceInfo();

    private RedirectorGateway redirectorGateway = RedirectorGateway.getInstance();


    @RequestMapping(value = "/version", method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ServiceInfo getVersion() {
        return serviceInfo;
    }

    @RequestMapping(value = "/healthCheck", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getHealthCheck() {
        try {
            redirectorGateway.getApplications();
        } catch (Exception e) {
            LOGGER.error("", e);
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    private ServiceInfo obtainServiceInfo() {
        try {
            Configuration config = new PropertiesConfiguration(ServiceInfo.CONFIG_FILE_NAME);
            return new ServiceInfo(config);
        } catch (ConfigurationException e) {
            LOGGER.warn("Failed to load configuration file: {}", ServiceInfo.CONFIG_FILE_NAME);
        } catch (Exception e) {
            LOGGER.warn("Exception appears while configuration file is loading", e);
        }
        return null;
    }
}
