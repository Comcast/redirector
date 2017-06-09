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

public interface Validator<T> {
    enum ValidationResultType {
        SUCCESS,
        FAILURE,
        REQUIRED_MODEL_NULL,
        REQUIRED_MODEL_INVALID,
        NON_ACCEPTABLE_DELTA,
        CANT_REDIRECT_TO_DEFAULT,
        CANT_REDIRECT_TO_FALLBACK,
        NOT_ENOUGH_HOSTS_FOR_DEFAULT,
        CANT_SYNC_MODELS_METADATA_FROM_DATASTORE,
        SKIP_SYNC_MODELS_AS_THEY_ALREADY_IN_SYNC,
        TOO_MUCH_DEVIATION,
        ALLOWABLE_DEVIATION}

    ValidationReport validate(T model);
}
