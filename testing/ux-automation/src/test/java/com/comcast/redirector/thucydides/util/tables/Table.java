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
 */
package com.comcast.redirector.thucydides.util.tables;

import net.thucydides.core.annotations.findby.By;
import org.openqa.selenium.WebElement;

public class Table {

    private WebElement table;

    public Table(WebElement table) {
        this.table = table;
    }

    public WebElement getEditButton(String testCaseName) {
        return table.findElement(By.cssSelector("td#" + testCaseName + " button.test-suite-edit-button"));
    }

    public WebElement getActionsButton(String testCaseName) {
        return table.findElement(By.cssSelector("td#" + testCaseName + " button.test-suite-action-button"));
    }

    public WebElement getDeleteButton(String testCaseName) {
        return table.findElement(By.cssSelector("td#" + testCaseName + " a.test-suite-delete-button"));
    }
}
