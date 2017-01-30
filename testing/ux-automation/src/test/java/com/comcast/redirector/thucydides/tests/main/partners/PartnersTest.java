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
 * @author Roman Dolomansky (rdolomansky@productengine.com)
 */

package com.comcast.redirector.thucydides.tests.main.partners;

import com.comcast.redirector.api.redirector.helpers.ServiceHelper;
import com.comcast.redirector.common.DeciderConstants;
import com.comcast.redirector.thucydides.steps.GenericPageSteps;
import com.comcast.redirector.thucydides.steps.partners.PartnersSteps;
import com.comcast.redirector.thucydides.util.GenericTestUtils;
import com.comcast.redirector.thucydides.util.TestConstants;
import net.thucydides.core.Thucydides;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.ManagedPages;
import net.thucydides.core.annotations.Steps;
import net.thucydides.core.pages.Pages;
import net.thucydides.junit.runners.ThucydidesRunner;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.WebDriver;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.File;


@RunWith(ThucydidesRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PartnersTest {
    private static final String PARTNER_ID = "test";
    private static final File dirForDownload = GenericTestUtils.createTempDir();

    @ManagedPages(defaultUrl = TestConstants.DECIDER_MAIN_UX_URL)
    private Pages pages;

    @Managed(uniqueSession = false)
    private WebDriver webdriver;

    @Steps
    private PartnersSteps partnersSteps;

    @Steps
    private GenericPageSteps genericPageSteps;

    @BeforeClass
    public static void setup() {
        Thucydides.useFirefoxProfile(GenericTestUtils.createProfileNeverAskSaving(dirForDownload.getAbsolutePath()));
    }

    @Test
    public void addPartner() throws InterruptedException {
        createPartner(PARTNER_ID);
        deletePartner(PARTNER_ID);
    }

    @Test
    public void editPartner() throws InterruptedException {
        createPartner(PARTNER_ID);

        final String newName = "newKey";
        final String newValue = "newValue";

        partnersSteps
                .clickEditPartnerButton(PARTNER_ID)
                .setName(newName)
                .setValue(newValue)
                .clickAddPropertyButton()
                .clickSaveButton()
                .clickEditPartnerButton(PARTNER_ID)
                .isPropertyExist(newName, newValue)
                .removePropertyById(newName)
                .clickSaveButton();

        deletePartner(PARTNER_ID);
    }

    @Test
    public void exportAllPartner() throws Exception {
        createPartner(PARTNER_ID);

        partnersSteps
                .openPage()
                .clickExportAllPartnerButton();

        genericPageSteps
                .checkSavedFile(dirForDownload, "exportedAllPartners.json");

        deletePartner(PARTNER_ID);
    }

    @Test
    public void exportPartner() throws Exception {
        createPartner(PARTNER_ID);

        partnersSteps
                .openPage()
                .clickExportPartnerButton(PARTNER_ID);

        genericPageSteps
                .checkSavedFile(dirForDownload, "exportedPartner_" + PARTNER_ID + ".json");

        deletePartner(PARTNER_ID);
    }

    private void createPartner(String id) {
        partnersSteps
                .openPage()
                .clickAddPartnerButton()
                .setId(id);

        final int propertySize = 5;

        // adding
        for (int i = 0; i < propertySize; i++) {
            partnersSteps
                    .setName("key_" + i)
                    .setValue("value_" + i)
                    .clickAddPropertyButton();
        }

        // checking
        for (int i = 0; i < propertySize; i++) {
            partnersSteps
                    .isPropertyExist("key_" + i, "value_" + i);
        }

        partnersSteps
                .clickSaveButton();
    }

    private void deletePartner(final String id) {
        partnersSteps.clickDeletePartnerButton(id);
        genericPageSteps.clickModalOkButton();
        partnersSteps.refreshPage();
    }
}
