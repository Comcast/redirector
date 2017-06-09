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

package com.comcast.redirector.thucydides.tests.main.namespaced;

import com.comcast.redirector.api.model.namespaced.NamespacedListType;
import com.comcast.redirector.thucydides.steps.namespaced.NamespacedAddPageUserSteps;
import com.comcast.redirector.thucydides.steps.namespaced.NamespacedFindPageUserSteps;
import com.comcast.redirector.thucydides.steps.namespaced.NamespacedShowPageUserSteps;
import com.comcast.redirector.thucydides.tests.UxTestSuite;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.Steps;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.WebDriver;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class NamespacedBaseTest {
    String EMPTY = "empty";
    String EDIT_EMPTY_VALUE = "edit_empty";
    String TEST = "test";
    String VALUE1 = "value1";
    String VALUE2 = "value2";
    String VALUE3 = "value3";
    String EDIT_TEST_VALUE = "edit_test";

    @Managed(uniqueSession = true)
    public WebDriver webdriver;

    @Steps
    NamespacedAddPageUserSteps namespacedAddSteps;

    @Steps
    NamespacedShowPageUserSteps namespacedShowSteps;

    @Steps
    private NamespacedFindPageUserSteps namespacedFindSteps;

    @Test
    public void _01_createEmptyNamespaced() {
        String listName = EMPTY + getSuffix();
        getNamespacedAddSteps()
            .openPage()
            .setName(listName)
            .setDescription(listName)
            .saveNamespaced();
    }

    @Test
    public void _02_editEmptyNamespaced() {
        String listName = EMPTY + getSuffix();
        namespacedShowSteps
            .openPage()
            .setNameFilter(listName)
            .editNamespaced(listName);

        getNamespacedAddSteps()
            .addValue(EDIT_EMPTY_VALUE + getValueSuffix())
            .saveNamespaced();
    }

    @Test
    public void _03_createNamespaced() {
        String listName = TEST + getSuffix();

        getNamespacedAddSteps()
            .openPage()
            .setName(listName)
            .setDescription(TEST)
            .addValue(VALUE1 + getValueSuffix())
            .addValue(VALUE2 + getValueSuffix())
            .addValue(VALUE3 + getValueSuffix())
            .saveNamespaced();
    }

    @Test
    public void _04_editNamespaced() {
        String listName = TEST + getSuffix();

        namespacedShowSteps
            .openPage()
            .setNameFilter(listName)
            .editNamespaced(listName);

        getNamespacedAddSteps()
            .addValue(EDIT_TEST_VALUE)
            .saveNamespaced();
    }

    @Test
    public void _05_deleteNamespaced() {
        String listName = TEST + getSuffix();

        namespacedShowSteps
            .openPage()
            .setNameFilter(listName)
            .deleteNamespaced(listName)
            .confirmDeletionNamespaced();
    }

    /**
     * Note: it relies on previous tests, new namespace isn't added
     * @throws InterruptedException
     */
    @Test
    public void _06_findNamespaced() throws InterruptedException {
        namespacedFindSteps.openPage()
                .typeSearchValue(UxTestSuite.Constants.NAMESPACED_LIST)
                .clickSearchButton()
                .verifyNamespacedListName(UxTestSuite.Constants.NAMESPACED_LIST)
                .clickShowFlavorRulesButton()
                .verifyRuleName(UxTestSuite.Constants.RULE_FLAVOR);
    }

    @Test
    public void _07_findNamespacedByValue() throws InterruptedException {
        namespacedFindSteps.openPage()
                .typeSearchValue(EDIT_EMPTY_VALUE + getValueSuffix())
                .clickSearchButton()
                .verifyNamespacedListName(EMPTY + getSuffix());
    }

    @Test
    public void _08_resolveNamespacedDuplicates() throws InterruptedException {
        String listName = TEST + getSuffix();

        namespacedAddSteps.openPage()
            .setName(listName)
            .setDescription(TEST)
            .addValue(VALUE1 + getValueSuffix())
            .addValue(VALUE2 + getValueSuffix())
            .saveNamespaced()

            .openPage()
            .setName(listName + "_duplicates")
            .setDescription(TEST)
            .addValue(VALUE1 + getValueSuffix())
            .addValue(VALUE2 + getValueSuffix())
            .tryToSaveNamespaced()

            .deleteDuplicateFromCandidateList(VALUE1 + getValueSuffix())
            .deleteDuplicateFromExistingList(VALUE2 + getValueSuffix())
            .saveNamespacedListFromDuplicatesModal();
    }

    @Test
    public void _09_testTypeSelector() throws InterruptedException {

        namespacedShowSteps.openPage()
            .selectOneTypeSelector(getSelectorForNamespacedListType())
            .editFirstNamespaced();

        namespacedAddSteps.verifyPageHasListType(getVisibleNamespacedListType());
    }

    /**
     * Suffix for name and description
     * @return
     */
    abstract String getSuffix();

    abstract String getValueSuffix();

    abstract NamespacedListType getSelectorForNamespacedListType();

    abstract String getVisibleNamespacedListType();

    protected NamespacedAddPageUserSteps getNamespacedAddSteps() {
        return namespacedAddSteps;
    }
}
