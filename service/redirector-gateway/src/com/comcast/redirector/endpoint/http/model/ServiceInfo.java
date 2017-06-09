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
 * @author Roman Dolomansky (rdolomansky@productengine.com)
 */

package com.comcast.redirector.endpoint.http.model;


import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

@XmlRootElement
public class ServiceInfo {

    private static final Logger log = LoggerFactory.getLogger(ServiceInfo.class);
    public static final String CONFIG_FILE_NAME = "dataservice.version.properties";
    public static final String SOURCE_KEY = "URL";
    public static final String PROJECT_NAME_KEY = "ProjectName";
    public static final String REVISION_KEY = "Revision";
    public static final String NA = "N/A";

    public String projectName;
    public String projectVersion = NA;
    public String serviceName = NA;
    public String serviceVersion = NA;
    public String source;
    public String rev;
    public String gitBranch = NA;
    public String gitBuildTime = NA;
    public String gitCommitId = NA;
    public String gitCommitTime = NA;

    public ServiceInfo() {
    }

    public ServiceInfo(String projectName, String source, String rev) {
        this.projectName = projectName;
        this.source = source;
        this.rev = rev;
    }

    static String getString(Configuration config, String key) {
        String value = StringUtils.trimToNull(config.getString(key));
        return value == null ? NA : value;
    }

    public ServiceInfo(Configuration config) {
        // read the maven properties
        projectName = getString(config, PROJECT_NAME_KEY);
        projectVersion = getString(config, "ProjectVersion");

        // read the sub-version properties
        rev = getString(config, REVISION_KEY);
        source = config.getString(SOURCE_KEY);
        if (source != null && !source.isEmpty()) {
            try {
                URI uri = new URI(source);
                source = uri.getPath();
            } catch (Exception e) {
                log.warn("An error occurs while process source URI " + source, e);
                source = NA;
            }
        } else {
            source = NA;
        }

        // read the git properties
        gitBranch = getString(config, "git.branch");
        gitBuildTime = getString(config, "git.build.time");
        gitCommitId = getString(config, "git.commit.id");
        gitCommitTime = getString(config, "git.commit.time");

        // read the serviceName and serviceVersion set in wrapper.sh
        serviceName = System.getProperty("serviceName", NA);
        serviceVersion = System.getProperty("serviceVersion", NA);
    }
}
