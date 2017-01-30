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

package com.comcast.redirector.thucydides.pages;

import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;

public class GenericPageObjects extends PageObject {

    @FindBy(css = "div.modal-footer button")
    private WebElementFacade modalOkButton;

    @FindBy(css = "div.modal")
    private WebElementFacade modalWindow;

    @FindBy(css = "h4.modal-title")
    private WebElementFacade deleteModalTitle;

    @FindBy(css = "div.modal-body")
    private WebElementFacade deleteModalBody;

    @FindBy(id = "toast-container")
    private WebElementFacade toaster;

    public void waitForToasterToAppear() {
        toaster.waitUntilVisible();
    }

    public String getToasterText() {
        return toaster.getText();
    }

    public void clickModalOkButton() {
        modalOkButton.click();
    }

    public void waitForModalWindowToOpen() {
        modalWindow.waitUntilVisible();
    }

    public String getDeleteModalTitleText() {
        return deleteModalTitle.getText();
    }

    public String getDeleteModalBodyText() {
        return deleteModalBody.getText();
    }
}
