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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package com.comcast.redirector.api.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@XmlRootElement(name = "server")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Server extends VisitableExpression implements Serializable, Expressions {

    @XmlAttribute(name="isNonWhitelisted")
    private String isNonWhitelisted;

    @XmlElement(name = "name", required = true)
    private String name;
    private String url;
    private String path;
    private Map<String, String> query;
    private String description;

    public Server() {
    }

    public Server(String url) {
        this.url = url;
    }

    public static Server newInstance(Server server) {

        if (server == null) {
            return null;
        }

        Server serverCopy = new Server();

        serverCopy.setIsNonWhitelisted(server.getIsNonWhitelisted());
        serverCopy.setName(server.getName());
        serverCopy.setUrl(server.getUrl());
        serverCopy.setPath(server.getPath());
        serverCopy.setDescription(server.getDescription());

        if (server.getQuery() != null) {

            Map<String, String> newQuery = new HashMap<>(server.getQuery().size());
            for (Map.Entry entry : server.getQuery().entrySet()) {
                newQuery.put((String)entry.getKey(), (String)entry.getValue());
            }

            serverCopy.setQuery(newQuery);
        }


        return serverCopy;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIsNonWhitelisted() {
        return isNonWhitelisted;
    }

    public void setIsNonWhitelisted(String isNonWhitelisted) {
        this.isNonWhitelisted = isNonWhitelisted;
    }

    public Map<String, String> getQuery() {
        return query;
    }

    public void setQuery(Map<String, String> query) {
        this.query = query;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Server server = (Server) o;
        return Objects.equals(isNonWhitelisted, server.isNonWhitelisted) &&
                Objects.equals(name, server.name) &&
                Objects.equals(url, server.url) &&
                Objects.equals(path, server.path) &&
                Objects.equals(query, server.query) &&
                Objects.equals(description, server.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isNonWhitelisted, name, url, path, query, description);
    }
}
