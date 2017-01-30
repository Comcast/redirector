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

package com.comcast.apps.e2e;

import com.comcast.apps.e2e.helpers.FilesPathHelper;
import com.comcast.apps.e2e.helpers.ServiceHelper;
import com.comcast.apps.e2e.managers.CommonEntityLoader;
import com.comcast.apps.e2e.managers.ModelEntityLoader;
import com.comcast.apps.e2e.managers.SetUpCommonEnvManager;
import com.comcast.apps.e2e.managers.SetUpEnvManager;
import com.comcast.apps.e2e.runners.ITestCasesRunner;
import com.comcast.apps.e2e.tasks.*;
import com.comcast.apps.e2e.utils.FileUtil;

public class E2EFacade {
    private final TaskChain globalDataInitTaskChain;
    private final TaskChain setupAndRunTestForAppTaskChain;
    private final Context context;

    private E2EFacade(Builder builder) {
        context = builder.context;
        globalDataInitTaskChain = builder.commonTaskChain;
        setupAndRunTestForAppTaskChain = builder.processTaskChain;
    }

    public boolean initCommonData() throws Exception {
        return globalDataInitTaskChain.execute(context);
    }

    public boolean runTests() throws Exception {
        return setupAndRunTestForAppTaskChain.execute(context);
    }

    public static class TaskChainFactory {
        private final Context context;
        private final FileUtil fileUtil;
        private final ITestCasesRunner testCasesRunner;
        private final FilesPathHelper filesPathHelper;

        protected TaskChainFactory(Builder builder) {
            fileUtil = builder.fileUtil;
            filesPathHelper = builder.filesPathHelper;
            context = builder.context;
            testCasesRunner = builder.testCasesRunner;
        }

        public TaskChain createGlobalDataInitTaskChain() {
            return new TaskChain(new LoadInputDataTask(new CommonEntityLoader(fileUtil, filesPathHelper, context)))
                    .and(new DynamicStackRegistrationTask())
                    .and(new SetUpEnvTask(new SetUpCommonEnvManager(context, new ServiceHelper(context.getBaseUrl()))));
        }

        public TaskChain createSetupAndRunTestForAppTaskChain() {
            return new TaskChain(new StartDispatcherTask())
                    .and(new LoadInputDataTask(new ModelEntityLoader(fileUtil, filesPathHelper, context)))
                    .and(new StaticStackRegistrationTask())
                    .and(new SetUpEnvTask(new SetUpEnvManager(context, new ServiceHelper(context.getBaseUrl()))))
                    .and(new GenerateTestCasesTask())
                    .and(new RunTestCasesTask(testCasesRunner, filesPathHelper))
                    .and(new ReportTask(fileUtil, filesPathHelper));
        }
    }

    public static class Builder {
        private TaskChainFactory taskChainFactory;
        private FileUtil fileUtil;
        private FilesPathHelper filesPathHelper;
        private Context context;
        private TaskChain commonTaskChain;
        private TaskChain processTaskChain;
        private ITestCasesRunner testCasesRunner;

        public Builder setFileUtil(FileUtil fileUtil) {
            this.fileUtil = fileUtil;
            return this;
        }

        public Builder setFilesPathHelper(FilesPathHelper filesPathHelper) {
            this.filesPathHelper = filesPathHelper;
            return this;
        }

        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        public Builder setTestCasesRunner(ITestCasesRunner testCasesRunner) {
            this.testCasesRunner = testCasesRunner;
            return this;
        }

        public E2EFacade build() {
            taskChainFactory = new TaskChainFactory(this);
            processTaskChain = taskChainFactory.createSetupAndRunTestForAppTaskChain();
            commonTaskChain = taskChainFactory.createGlobalDataInitTaskChain();
            return new E2EFacade(this);
        }
    }
}
