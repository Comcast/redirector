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
 * @author Stanislav Menshykov (smenshykov@productengine.com)
 */
package com.comcast.redirector.common.util;

import com.comcast.redirector.common.logging.ExecutionFlow;
import com.comcast.redirector.common.RedirectorConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadLocalLogger {

    private static ThreadLocal<String> executionStep = new ThreadLocal<>();
    private static ThreadLocal<String> executionFlow = new ThreadLocal<>();
    private static ThreadLocal<String> customMessage = new ThreadLocal<>();
    private Logger loggingProvider;

    public ThreadLocalLogger(Logger loggingProvider) {
        this.loggingProvider = loggingProvider;
    }

    public ThreadLocalLogger(Class clazz) {
        this.loggingProvider = LoggerFactory.getLogger(clazz);
    }

    public static void setExecutionFlow(ExecutionFlow executionFlow) {
        setExecutionFlow(executionFlow.toString());
    }

    public static void setExecutionFlow(String executionFlow) {
        if (StringUtils.isEmpty(ThreadLocalLogger.executionFlow.get())) {
            ThreadLocalLogger.executionFlow.set(RedirectorConstants.Logging.EXECUTION_FLOW_PREFIX + executionFlow);
        } else {
            ThreadLocalLogger.executionStep.set(RedirectorConstants.Logging.EXECUTION_STEP_PREFIX + executionFlow);
        }

    }

    public static void setCustomMessage(String customMessage) {
        ThreadLocalLogger.customMessage.set(customMessage);
    }

    public static void clear() {
        ThreadLocalLogger.executionFlow.set(null);
        ThreadLocalLogger.executionStep.set(null);
        ThreadLocalLogger.customMessage.set(null);
    }

    public void info(String message, Object... arguments) {
        loggingProvider.info(appendToLog(message), arguments);
    }

    public void error(String message, Object... arguments) {
        loggingProvider.error(appendToLog(message), arguments);
    }

    public void warn(String message, Object... arguments) {
        loggingProvider.warn(appendToLog(message), arguments);
    }

    public void debug(String message, Object... arguments) {
        loggingProvider.debug(appendToLog(message), arguments);
    }

    private String appendToLog(String message) {
        String customMessage = ThreadLocalLogger.customMessage.get();
        String executionStep = ThreadLocalLogger.executionStep.get();
        String executionFlow = ThreadLocalLogger.executionFlow.get();
        if (StringUtils.isNotEmpty(customMessage)) {
            message = customMessage + " " + message;
        }
        if (StringUtils.isNotEmpty(executionStep)) {
            message = executionStep + " " + message;
        }
        if (StringUtils.isNotEmpty(executionFlow)) {
            message = executionFlow + " " + message;
        }

        return message;
    }
}
