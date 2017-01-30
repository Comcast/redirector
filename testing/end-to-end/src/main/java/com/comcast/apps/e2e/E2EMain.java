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
 * @author Stanislav Menshykov (smenshykov@productengine.com)
 */

package com.comcast.apps.e2e;

import com.comcast.apps.e2e.config.E2EConfigLoader;
import com.comcast.apps.e2e.extratests.IExtraTest;
import com.comcast.apps.e2e.runners.ITestCasesRunner;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Automation of different levels of testing. We create rules on clean ZK and then run set of tests on XRE Redirector and then get a report
 * Basic steps:
 * 1. Validate properties for EETT
 * 2. Cleanup zookeeper for EETT
 * 3. Load and setup common data(for example NS lists, redirector config)
 * 4. Run tests for all applications from file applications.json
 * 4.1. EETT starts a zookeeper reload dispatcher for a specific application
 * 4.2. EETT starts a loading data from backup files for a specific application
 * 4.3. EETT starts a registration of stacks for a specific application
 * 4.4. EETT setups rules, distributions etc for a specific application
 * 4.5. EETT generates test cases for a specific application
 * 4.6. EETT prepares multiruntime_config.xml based on TCs retrieved in 4.5
 * 4.7. EETT runs CLR with multiruntime_config.xml from 4.6
 * 5. Once CRL session is finished EETT calls report servlet from XRE Redirector and obtain actual results + run logs for all tests being run
 * 6. EETT compares actual results from 5 with expected results from 4.5 and generates testing report
 * 7. When all reports are ready. EETT finishes
 */
public class E2EMain {

    public static void main(String args[]) throws Exception {
        E2ERunner.runTests(getRunner(), getExtraTests());
    }

    private static ITestCasesRunner getRunner() {
        return createInstance("com.comcast.apps.e2e.runners.", E2EConfigLoader.getDefaultInstance().getTestCasesRunner(), ITestCasesRunner.class);
    }

    private static List<IExtraTest> getExtraTests() {
        return E2EConfigLoader.getDefaultInstance().getExtraTestsClasses().stream()
                .map(simpleName -> createInstance("com.comcast.apps.e2e.extratests.", simpleName, IExtraTest.class))
                .collect(Collectors.toList());
    }

    private static <T> T createInstance(String packageName, String simpleName, Class<T> type) {
        try {
            String className = packageName + simpleName;
            Class<T> clazz = (Class<T>) Class.forName(className);
            return clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
