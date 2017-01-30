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

import com.comcast.apps.e2e.config.E2EConfigLoader;
import com.comcast.apps.e2e.extratests.ExtraTestReportList;
import com.comcast.apps.e2e.extratests.IExtraTest;
import com.comcast.apps.e2e.helpers.FilesPathHelper;
import com.comcast.apps.e2e.managers.ApplicationsLoader;
import com.comcast.apps.e2e.runners.ITestCasesRunner;
import com.comcast.apps.e2e.tasks.Context;
import com.comcast.apps.e2e.utils.CommonUtils;
import com.comcast.apps.e2e.utils.FileUtil;
import com.comcast.apps.e2e.utils.DataSourceUtil;
import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.common.serializers.JAXBContextBuilder;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.XMLSerializer;
import com.comcast.redirector.common.serializers.core.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.comcast.redirector.common.function.Wrappers.unchecked;

public class E2ERunner {
    private static final Logger log = LoggerFactory.getLogger(E2ERunner.class);

    private final Serializer jsonSerializer = new JsonSerializer();
    private final FileUtil fileUtil = new FileUtil(jsonSerializer, new XMLSerializer(new JAXBContextBuilder().createContextForXML()));

    private final FilesPathHelper filesPathHelper;
    private final String webServiceBaseUrl;

    private Set<String> appNamesToBeTested = Collections.emptySet();
    private ExecutorService executorService;

    private ITestCasesRunner testCasesRunner;
    private List<IExtraTest> extraTests;
    private List<Callable<Boolean>> jobs;

    private E2ERunner(String backupFilesFolderPath, String webServiceBaseUrl, ITestCasesRunner testCasesRunner, List<IExtraTest> extraTests) {
        filesPathHelper = new FilesPathHelper(backupFilesFolderPath);
        this.webServiceBaseUrl = webServiceBaseUrl;
        this.testCasesRunner = testCasesRunner;
        this.extraTests = extraTests;
    }

    private void run() throws Exception {
        initEnvironment();
        CommonUtils.waitForHostCachesToUpdate();
        loadAppNames();
        initCommonData();
        createJobs(appNamesToBeTested, this::createTestJobForApp);
        createJobs(extraTests, this::createJobForExtraTest);
        runAllJobsOrExitWithRuntimeException();
        saveExtraTestsReports();
    }

    private void initEnvironment() {
        DataSourceUtil.init();
        try {
            DataSourceUtil.cleanUpDataSource();
        } catch (DataSourceConnectorException e) {
            log.error("Failed to cleanup DataSource", e);
        }

        eraseGeneratedBackups();
    }

    private void loadAppNames() {
        appNamesToBeTested = new ApplicationsLoader(fileUtil, filesPathHelper).loadAppNames();
    }

    private void eraseGeneratedBackups() {
        try {
            filesPathHelper.getGeneratedBackupFilenamesFromBaseFolder().forEach(unchecked(fileUtil::delete));
        } catch (IOException e) {
            log.error("Failed to erase generated backups", e);
            throw new RuntimeException(e);
        }
    }

    private void initCommonData() {
        Context globalContext = new Context(webServiceBaseUrl);

        E2EFacade endToEndCommonFacade = new E2EFacade.Builder().
                setContext(globalContext).
                setFileUtil(fileUtil).
                setFilesPathHelper(filesPathHelper).
                build();

        try {
            endToEndCommonFacade.initCommonData();
        } catch (Exception e) {
            log.error("Fail to init env for End2End testing", e);
            throw new RuntimeException(e);
        }
    }

    private <T> void createJobs(Collection<T> tasks, Function<T, Callable<Boolean>> jobGeneratingFunction) throws Exception {
        if (jobs == null) {
            jobs = new ArrayList<>();
        }
        jobs.addAll(tasks.stream()
                .map(jobGeneratingFunction)
                .collect(Collectors.toList()));
    }

    private void saveExtraTestsReports() throws Exception {
        log.info("Saving extra tests report");

        ExtraTestReportList extraTestReportList = new ExtraTestReportList();
        extraTestReportList.setReports(extraTests.stream().map(IExtraTest::getReport).filter(Objects::nonNull).collect(Collectors.toList()));

        String fileName = filesPathHelper.getFilename(FilesPathHelper.TestEntity.EXTRA_TESTS_REPORT, FilesPathHelper.EXTRA_TESTS_BASE_DIR_NAME);
        fileUtil.writeJson(fileName, extraTestReportList);

        log.info("Extra tests report saved");
    }

    private Callable<Boolean> createTestJobForApp(String appName) {
        Context appContext = new Context(appName, webServiceBaseUrl);

        E2EFacade e2EFacade = new E2EFacade.Builder().
                setContext(appContext).
                setFileUtil(fileUtil).
                setTestCasesRunner(testCasesRunner).
                setFilesPathHelper(filesPathHelper).
                build();

        return () -> {
            try {
                CommonUtils.waitForHostCachesToUpdate();
                return e2EFacade.runTests();
            } catch (Exception e) {
                log.error("Failed to run tests for " + appName, e);
                return false;
            }
        };
    }

    private Callable<Boolean> createJobForExtraTest(IExtraTest extraTest) {
        return () -> {
            try {
                log.info("Started " + extraTest.getName() + " test");
                boolean result = extraTest.runTest();
                log.info(extraTest.getName() + (result ? " passed" : " failed"));
                return result;
            } catch (Exception e) {
                log.error("Failed to run test " + extraTest.getName(), e);
                return false;
            }
        };
    }

    private void runAllJobsOrExitWithRuntimeException() {
        executorService = Executors.newFixedThreadPool(jobs.size());
        List<Future<Boolean>> results;

        try {
            results = executorService.invokeAll(jobs);
        } catch (InterruptedException e) {
            log.error("Failed to execute tests", e);
            throw new RuntimeException(e);
        }

        if (isCompleteAllTasks(jobs, results)) {
            log.info("All tests are completed");
        }
    }

    private <T> boolean isCompleteAllTasks(Collection<T> tasks, List<Future<Boolean>> futures) {
        return futures.size() == tasks.size();
    }

    public static void runTests(ITestCasesRunner testCasesRunner, List<IExtraTest> extraTests) throws Exception {
        String basePath = E2EConfigLoader.getDefaultInstance().getBasePath();
        String baseUrl = E2EConfigLoader.getDefaultInstance().getServiceBaseUrl();
        String webApplicationBaseUrl = E2EConfigLoader.getDefaultInstance().getWebApplicationBaseUrl();
        String webApplicationBasePort = E2EConfigLoader.getDefaultInstance().getWebApplicationBasePort();

        LocalHttpService localHttpService = new LocalHttpService(basePath, webApplicationBaseUrl, webApplicationBasePort);
        localHttpService.start();

        new E2ERunner(basePath, baseUrl, testCasesRunner, extraTests).run();
    }
}
