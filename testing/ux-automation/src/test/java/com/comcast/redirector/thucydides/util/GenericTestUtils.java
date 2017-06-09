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
 * @author Maxym Dolina (mdolina@productengine.com)
 */

package com.comcast.redirector.thucydides.util;

import com.comcast.redirector.api.model.xrestack.ServicePaths;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.thucydides.tests.UxTestSuite;
import com.google.common.io.Files;
import org.junit.Assert;
import net.thucydides.core.annotations.findby.By;
import net.thucydides.core.pages.PageObject;
import org.openqa.selenium.firefox.FirefoxProfile;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.util.concurrent.TimeUnit;

import static com.comcast.redirector.thucydides.util.TestConstants.*;
import static com.comcast.redirector.common.RedirectorConstants.*;

public class GenericTestUtils {

    private static final int TICK_TIME = 100;

    public static FirefoxProfile createProfileNeverAskSaving(String pathToDownloadDir) {
        FirefoxProfile result = new FirefoxProfile();
        result.setPreference("browser.download.folderList", 2);
        result.setPreference("browser.download.manager.showWhenStarting", false);
        result.setPreference("browser.download.dir", pathToDownloadDir);
        result.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/xml, application/json, application/vnd.ms-excel");

        return result;
    }

    public static File createTempDir() {
        File tempDir = Files.createTempDir();
        tempDir.deleteOnExit();
        return tempDir;
    }

    public static By waitToBePresent(PageObject page, String selector) {
        By result = By.jquery(selector);
        page.shouldBeVisible(result);
        return result;
    }

    public static void waitForStacksToBePresentOnServer(int secondsToWait) throws InterruptedException {
        long waitTimeLimit = secondsToWait * 1000 + System.currentTimeMillis();
        while (true) {
            if (waitTimeLimit < System.currentTimeMillis()) {
                Assert.fail("Unable to get stacks from server");
            }

            if (!getServicePaths().getPaths().isEmpty())
                break;

            TimeUnit.MILLISECONDS.sleep(TICK_TIME);
        }
    }

    public static void waitForStacksToBePresentOnServer() throws InterruptedException {
        waitForStacksToBePresentOnServer(5);
    }

    private static ServicePaths getServicePaths() {
        String pathsForApp = DATA_SERVICE_BASE_URL + STACKS_CONTROLLER_PATH + DELIMETER + UxTestSuite.Constants.APP_NAME;
        HttpTestServerHelper.initWebTarget(pathsForApp);

        return HttpTestServerHelper.target().request(MediaType.APPLICATION_JSON).get().readEntity(ServicePaths.class);
    }
}
