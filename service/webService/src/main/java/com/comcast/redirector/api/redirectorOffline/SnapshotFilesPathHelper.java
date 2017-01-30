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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.api.redirectorOffline;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SnapshotFilesPathHelper {

    public enum SnapshotEntity {
        MODEL,
        APPLICATIONS,
        NAMESPACED_LISTS,
        CONFIG
    }

    private String appName;
    private String basePath;

    private static final Map<SnapshotEntity, String> filenames = new HashMap<SnapshotEntity, String>() {{
                put(SnapshotEntity.MODEL, "modelSnapshot.json");
                put(SnapshotEntity.APPLICATIONS, "applications.json");
                put(SnapshotEntity.NAMESPACED_LISTS, "namespacedlists.json");
                put(SnapshotEntity.CONFIG, "redirectorConfig.json");
            }};

    public SnapshotFilesPathHelper(String appName, String basePath) {
        this.appName = appName;
        this.basePath = basePath;
    }

    public SnapshotFilesPathHelper(String basePath) {
        this.basePath = basePath;
    }

    public String getFilename(SnapshotEntity backupEntity) {
        if (!StringUtils.isBlank(basePath)) {
            return (filenames.containsKey(backupEntity))
                    ? Joiner.on(File.separator).skipNulls().join(basePath, appName, filenames.get(backupEntity)) : null;
        } else {
            return null;
        }
    }
}
