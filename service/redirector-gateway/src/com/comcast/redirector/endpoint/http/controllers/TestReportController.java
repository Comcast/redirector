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

package com.comcast.redirector.endpoint.http.controllers;

import com.comcast.redirector.RedirectorGateway;
import com.comcast.redirector.api.model.testsuite.SessionList;
import com.comcast.redirector.core.engine.ILoggable;
import com.comcast.redirector.core.engine.IRedirector;
import com.comcast.redirector.core.engine.RedirectorImpl;
import com.comcast.redirector.endpoint.http.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Constants.SERVICE_URL_PREFIX + "/testReport")
public class TestReportController {
    private static final Logger log = LoggerFactory.getLogger(TestReportController.class);

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public SessionList getReport(@RequestParam String appName) {
        return generateReportData(appName);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public void deleteReport(@RequestParam String appName) {
        ILoggable loggable = getLog(appName);
        if (loggable != null) {
            loggable.getLog().clearAll();
        }
    }

    private SessionList generateReportData(String appName) {
        ILoggable loggable = getLog(appName);
        SessionList report;
        if (loggable != null) {
            report = loggable.getLog().pollAll();
            if (report == null) {
                report = new SessionList();
                log.warn("Failed to get session log for {}", appName);
            } else {
                log.info("Report is produced successfully for {}", appName);
            }
        } else {
            report = new SessionList();
        }

        return report;
    }

    private ILoggable getLog(String appName) {
        IRedirector redirector = RedirectorGateway.getInstance().getRedirector(appName);

        if (redirector instanceof RedirectorImpl && ((RedirectorImpl)redirector).getLog() != null) {
            return ((RedirectorImpl)redirector).getLog();
        } else {
            log.warn("Redirector is not yet initialized for {}", appName);
            return null;
        }
    }
}
