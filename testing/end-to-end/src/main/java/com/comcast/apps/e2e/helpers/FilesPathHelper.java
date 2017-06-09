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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.apps.e2e.helpers;

import com.comcast.apps.e2e.utils.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FilesPathHelper {

    public enum TestEntity {
        APPLICATIONS,
        MULTI_RUNTIME_CONFIG,
        GUIDE_STARTUP,
        NAMESPACED_LISTS,
        STATIC_STACKS,
        DYNAMIC_STACKS,
        WHITELISTED,
        SELECT_SERVER,
        URL_RULES,
        REDIRECTOR_CONFIG,
        REPORT,
        EXTRA_TESTS_REPORT
    }

    public static final String EXTRA_TESTS_BASE_DIR_NAME = "extraTests";

    private String basePath;

    private static final Map<TestEntity, String> filenames = new HashMap<TestEntity, String>() {{
        put(TestEntity.APPLICATIONS, "applications.json");
        put(TestEntity.MULTI_RUNTIME_CONFIG, "multiruntime_config.xml");
        put(TestEntity.GUIDE_STARTUP, "guide_startup.xml");
        put(TestEntity.NAMESPACED_LISTS, "namespacedlists.json");
        put(TestEntity.STATIC_STACKS, "manualbackup.json");
        put(TestEntity.DYNAMIC_STACKS, "stacks.json");
        put(TestEntity.WHITELISTED, "whitelist.xml");
        put(TestEntity.SELECT_SERVER, "selectserver.xml");
        put(TestEntity.URL_RULES, "urlrules.xml");
        put(TestEntity.REDIRECTOR_CONFIG, "redirectorConfig.json");
        put(TestEntity.REPORT, "report.json");
        put(TestEntity.EXTRA_TESTS_REPORT, "extraTestsReport.json");
    }};

    public FilesPathHelper(String basePath) {
        this.basePath = basePath;
    }

    public Set<String> getGeneratedBackupFilenamesFromBaseFolder() throws IOException { // TODO: return stream
        Set<String> filenames = Files.list(Paths.get(basePath))
                .filter(Files::isDirectory)
                .map(Path::getFileName).map(Path::toString)
                .flatMap(appName -> Stream.of(
                        getFilename(TestEntity.MULTI_RUNTIME_CONFIG, appName),
                        getFilename(TestEntity.REPORT, appName)))
                .collect(Collectors.toSet());
        return filenames;
    }

    public String getFilename(TestEntity testEntity) {
        return (filenames.containsKey(testEntity)) ? Stream.of(basePath, filenames.get(testEntity)).collect(Collectors.joining(File.separator)) : null;
    }

    public String getFilename(TestEntity testEntity, String serviceName) {
        return (filenames.containsKey(testEntity)) ? Stream.of(basePath, serviceName, filenames.get(testEntity)).collect(Collectors.joining(File.separator)) : null;
    }
}
