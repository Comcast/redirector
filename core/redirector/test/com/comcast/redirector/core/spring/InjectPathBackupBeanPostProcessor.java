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
 */
package com.comcast.redirector.core.spring;

import com.comcast.xre.common.redirector.selector.xmlimpl.Config;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class InjectPathBackupBeanPostProcessor implements BeanPostProcessor {
    private String backupBasePath;

    public void setBackupBasePath (String backupBasePath) {
        this.backupBasePath = backupBasePath;
    }

    @Override
    public Object postProcessBeforeInitialization (Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization (Object bean, String beanName) throws BeansException {
        if (beanName.equals("config") && StringUtils.isNotBlank(backupBasePath)) {
            ((Config) bean).setBackupBasePath(backupBasePath);
        }
        return bean;
    }
}
