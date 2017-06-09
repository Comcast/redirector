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
package com.comcast.redirector.core.config;


public class TestLoadConfigFailed {

    private Long testLong;
    private Boolean testBoolean;
    private String testString;
    private Integer testInteger;

    public TestLoadConfigFailed (Long testLong, Boolean testBoolean, String testString, Integer testInteger) {
        this.testLong = testLong;
        this.testBoolean = testBoolean;
        this.testString = testString;
        this.testInteger = testInteger;
    }

    public Long getTestLong () {
        return testLong;
    }

    public void setTestLong (Long testLong) {
        this.testLong = testLong;
    }

    public Boolean getTestBoolean () {
        return testBoolean;
    }

    public void setTestBoolean (Boolean testBoolean) {
        this.testBoolean = testBoolean;
    }

    public String getTestString () {
        return testString;
    }

    public void setTestString (String testString) {
        this.testString = testString;
    }

    public Integer getTestInteger () {
        return testInteger;
    }

    public void setTestInteger (Integer testInteger) {
        this.testInteger = testInteger;
    }
}
