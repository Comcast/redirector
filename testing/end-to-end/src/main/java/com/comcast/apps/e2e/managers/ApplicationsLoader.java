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

package com.comcast.apps.e2e.managers;

import com.comcast.apps.e2e.helpers.FilesPathHelper;
import com.comcast.apps.e2e.utils.FileUtil;
import com.comcast.redirector.core.applications.Applications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class ApplicationsLoader {
    private static final Logger log = LoggerFactory.getLogger(ApplicationsLoader.class);

    private final FileUtil fileUtil;
    private final FilesPathHelper filesPathHelper;

    public ApplicationsLoader(FileUtil fileUtil, FilesPathHelper filesPathHelper) {
        this.fileUtil = fileUtil;
        this.filesPathHelper = filesPathHelper;
    }

    public Set<String> loadAppNames() {
        String backupFileName = filesPathHelper.getFilename(FilesPathHelper.TestEntity.APPLICATIONS);
        Applications applications = fileUtil.readJson(backupFileName, Applications.class);
        if(applications == null){
            log.error("Failed load applications from backup files");
            throw new IllegalArgumentException("Failed load applications from backup files");
        }
        return applications.getApps();
    }
}
