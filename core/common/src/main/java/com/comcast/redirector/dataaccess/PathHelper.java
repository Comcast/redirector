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

package com.comcast.redirector.dataaccess;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class PathHelper implements IPathHelper {
    public static final String DELIMETER = "/";

    private String baseNodePath;
    private String entityPath;

    private static Map<String, IPathHelper> helperMap = new HashMap<>();

    public PathHelper(String basePath, String rootPath, String entityPath) {
        baseNodePath = basePath + rootPath;
        this.entityPath = entityPath;
    }

    public static synchronized IPathHelper getOrCreateHelper(String basePath, String rootPath, String entityPath) {
        String key = basePath + rootPath + entityPath;
        if (!helperMap.containsKey(key)) {
            helperMap.put(key, new PathHelper(basePath, rootPath, entityPath));
        }

        return helperMap.get(key);
    }

    @Override
    public String getPath(EntityType entityType) {
        return baseNodePath + DELIMETER + entityType.getPath();
    }

    @Override
    public String getPath(EntityType entityType, String... childId) {
        return getPathForChildren(getPath(entityType), childId);
    }

    @Override
    public String getRawPath(String... childId) {
        return baseNodePath + DELIMETER + entityPath + StringUtils.join(childId, DELIMETER);
    }

    @Override
    public String getPath() {
        return baseNodePath + getJoinedChildren(entityPath);
    }

    @Override
    public String getPath(String... childId) {
        return getPathForChildren(getPath(), childId);
    }

    @Override
    public String getPathByService(String serviceName) {
        return baseNodePath + DELIMETER + serviceName + getJoinedChildren(entityPath);
    }

    @Override
    public String getPathByService(String serviceName, String... childId) {
        return getPathForChildren(getPathByService(serviceName), childId);
    }

    @Override
    public String getPathByService(String serviceName, EntityType entityType, String... childId) {
        return baseNodePath + DELIMETER + serviceName + DELIMETER + entityType.getPath() + getJoinedChildren(StringUtils.join(childId));
    }

    private String getPathForChildren(String basePath, String... childId) {
        return basePath + getJoinedChildren(StringUtils.join(childId));
    }

    private String getJoinedChildren(String... childId) {
        String joined = StringUtils.join(childId, DELIMETER);
        return (StringUtils.isNotBlank(joined)) ? DELIMETER + joined : "";
    }

    public static IPathHelper getPathHelper(EntityType entityType, String basePath) {
        return getOrCreateHelper(basePath, entityType.getCategory().getRootPath(), entityType.getPath());
    }

    public static IPathHelper getPathHelper(EntityCategory entityCategory, String basePath) {
        return getOrCreateHelper(basePath, entityCategory.getRootPath(), null);
    }
}
