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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.apps.e2e.tasks;

import com.comcast.apps.e2e.helpers.FilesPathHelper;
import com.comcast.apps.e2e.helpers.ServiceHelper;
import com.comcast.apps.e2e.managers.ReportManager;
import com.comcast.apps.e2e.utils.FileUtil;
import com.comcast.redirector.api.model.testsuite.RedirectorTestCaseList;

public class ReportTask implements IProcessTask {

    private final FileUtil fileUtil;
    private final FilesPathHelper filesPathHelper;

    public ReportTask(FileUtil fileUtil, FilesPathHelper filesPathHelper) {
        this.fileUtil = fileUtil;
        this.filesPathHelper = filesPathHelper;
    }

    @Override
    public Boolean handle(Context context) throws Exception {
        String serviceName = context.getServiceName();
        RedirectorTestCaseList testCases = context.getRedirectorTestCaseList();
        ReportManager reportManager = new ReportManager(serviceName, fileUtil, filesPathHelper,
            new ServiceHelper(serviceName, context.getBaseUrlForReport()));
        reportManager.createReport(testCases);
        reportManager.clearSessionLog();
        return true;
    }
}
