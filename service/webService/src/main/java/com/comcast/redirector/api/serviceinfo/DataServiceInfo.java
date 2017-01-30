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
 */
package com.comcast.redirector.api.serviceinfo;


import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

@Singleton
@Path("/")
public class DataServiceInfo {
    private static final Logger log = LoggerFactory.getLogger(DataServiceInfo.class);
    public static final String CONFIG_FILE_NAME = "dataservice.version.properties";
    public static final String SOURCE_KEY = "URL";
    public static final String PROJECT_NAME_KEY = "ProjectName";
    public static final String REVISION_KEY = "Revision";
    public static final String NA = "N/A";
    public static final String OK = "Ok";
    public static final String URL_PATH_VERSION = "version";
    public static final String URL_PATH_HEART_BEAT = "heartBeat";

    protected ServiceInfo serviceInfo = obtainServiceInfo();

    @Autowired
    private IDataSourceConnector dataSourceConnector;


    @GET
    @Path("/version")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public ServiceInfo getVersion(@Context UriInfo ui) {
        return serviceInfo;
    }

    @GET
    @Path("/healthCheck")
    @Produces({MediaType.TEXT_PLAIN})
    public Response getHealthCheck() {
        if (!dataSourceConnector.isConnected()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
        return Response.status(Response.Status.OK).entity("OK").build();
    }

    private ServiceInfo obtainServiceInfo() {
        try {
            Configuration config = new PropertiesConfiguration(CONFIG_FILE_NAME);
            return new ServiceInfo(config);
        } catch (ConfigurationException ce) {
            log.warn("Failed to load configuration file: {}", CONFIG_FILE_NAME);
        } catch (Exception e) {
            log.warn("Exception appears while configuration file is loading", e);
        }
        return null;
    }

    @XmlRootElement
    public static class ServiceInfo {
        private String projectVersion = NA;
        private String serviceName = NA;
        private String serviceVersion = NA;
        private String source;
        private String rev;
        private String gitBranch = NA;
        private String gitBuildTime = NA;
        private String gitCommitId = NA;
        private String gitCommitTime = NA;
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

        public String getProjectName() {
            return projectName;
        }

        public void setProjectName(String projectName) {
            this.projectName = projectName;
        }

        private String projectName;

        public String getProjectVersion() {
            return projectVersion;
        }

        public void setProjectVersion(String projectVersion) {
            this.projectVersion = projectVersion;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getServiceVersion() {
            return serviceVersion;
        }

        public void setServiceVersion(String serviceVersion) {
            this.serviceVersion = serviceVersion;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getRev() {
            return rev;
        }

        public void setRev(String rev) {
            this.rev = rev;
        }

        public String getGitBranch() {
            return gitBranch;
        }

        public void setGitBranch(String gitBranch) {
            this.gitBranch = gitBranch;
        }

        public String getGitBuildTime() {
            return gitBuildTime;
        }

        public void setGitBuildTime(String gitBuildTime) {
            this.gitBuildTime = gitBuildTime;
        }

        public String getGitCommitId() {
            return gitCommitId;
        }

        public void setGitCommitId(String gitCommitId) {
            this.gitCommitId = gitCommitId;
        }

        public String getGitCommitTime() {
            return gitCommitTime;
        }

        public void setGitCommitTime(String gitCommitTime) {
            this.gitCommitTime = gitCommitTime;
        }

        @Override
        public String toString() {
            return "ServiceInfo {" + "\n" +
                    "  projectVersion='" + projectVersion + '\'' + "\n" +
                    "  serviceName='" + serviceName + '\'' + "\n" +
                    "  serviceVersion='" + serviceVersion + '\'' + "\n" +
                    "  source='" + source + '\'' + "\n" +
                    "  rev='" + rev + '\'' + "\n" +
                    "  gitBranch='" + gitBranch + '\'' + "\n" +
                    "  gitBuildTime='" + gitBuildTime + '\'' + "\n" +
                    "  gitCommitId='" + gitCommitId + '\'' + "\n" +
                    "  gitCommitTime='" + gitCommitTime + '\'' + "\n" +
                    "  projectName='" + projectName + '\'' + "\n" +
                    '}';
        }
    }
}
