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

package com.comcast.redirector.core.modelupdate.chain.validator;

import java.util.Objects;

public class ValidationReport {
    private Validator.ValidationResultType validationResultType;
    private String message;

    public ValidationReport(Validator.ValidationResultType validationResultType) {
        this.validationResultType = validationResultType;
    }

    public ValidationReport(Validator.ValidationResultType validationResultType, String message) {
        this.validationResultType = validationResultType;
        this.message = message;
    }

    public Validator.ValidationResultType getValidationResultType() {
        return validationResultType;
    }

    public void setValidationResultType(Validator.ValidationResultType validationResultType) {
        this.validationResultType = validationResultType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccessValidation() {
        return Validator.ValidationResultType.ALLOWABLE_DEVIATION == validationResultType || Validator.ValidationResultType.SUCCESS == validationResultType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationReport that = (ValidationReport) o;
        return Objects.equals(validationResultType, that.validationResultType) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(validationResultType, message);
    }
}
