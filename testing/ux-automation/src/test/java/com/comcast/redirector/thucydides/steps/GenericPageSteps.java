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
 * @author Maxym Dolina (mdolina@productengine.com)
 */

package com.comcast.redirector.thucydides.steps;

import com.comcast.redirector.thucydides.pages.GenericPageObjects;
import net.thucydides.core.annotations.Step;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GenericPageSteps {

    GenericPageObjects page;

    @Step
    public GenericPageSteps verifyDeleteModalWindow(String entity) {
        page.waitForModalWindowToOpen();
        String modalTitleText = page.getDeleteModalTitleText();
        assertTrue(modalTitleText.contains("Delete confirmation"));
        String modalBodyText = page.getDeleteModalBodyText();
        assertTrue(modalBodyText.contains("Are you sure you want to delete " + entity));
        return this;
    }

    @Step
    public GenericPageSteps clickModalOkButton() {
        page.clickModalOkButton();
        page.waitFor(300).milliseconds();
        return this;
    }

    @Step
    public GenericPageSteps waitSuccessfullySavedToaster(String savedEntityId) {
        waitSuccessToaster(page, "Saved " + savedEntityId);
        return this;
    }

    @Step
    public GenericPageSteps waitSuccessfullyDeletedToaster(String deletedEntityId) {
        waitSuccessToaster(page, deletedEntityId);
        return this;
    }

    @Step
    public GenericPageSteps checkSavedFile(File dirForDownload, String expectedFileName) throws Exception {
        Long startTime = System.currentTimeMillis();
        while (dirForDownload.listFiles().length < 1 && (System.currentTimeMillis() - startTime) < 1000) {
            //wait up to one second for file to be saved
        }
        File[] files = dirForDownload.listFiles();
        assertTrue(files.length == 1);
        File exportedFile = files[0];
        assertEquals(expectedFileName, exportedFile.getName());
        exportedFile.delete();
        return this;
    }

    private GenericPageSteps waitSuccessToaster(GenericPageObjects page, String content) {
        page.waitForToasterToAppear();
        String toasterText = page.getToasterText();
        assertTrue(toasterText.contains(content));
        assertTrue(toasterText.contains("Success"));
        return this;
    }
}
