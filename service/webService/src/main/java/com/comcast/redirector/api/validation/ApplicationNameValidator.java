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
package com.comcast.redirector.api.validation;

import com.comcast.redirector.api.redirector.service.IAppsService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class ApplicationNameValidator implements ConstraintValidator<VerifyApplicationExists, String> {
    private static Logger log = LoggerFactory.getLogger(ApplicationNameValidator.class);

    @Autowired
    private IAppsService appsService;

    @Override
    public void initialize(VerifyApplicationExists applicationNameValid) {

    }

    @Override
    public boolean isValid(String pathParam, ConstraintValidatorContext constraintValidatorContext) {
        if (StringUtils.isNotBlank(pathParam)) {
            if (appsService.isApplicationExists(pathParam)) {
                return true;
            }
        }
        log.warn("Couldn't find application: [" + pathParam + "]");
        return false;
    }
}
