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

package it;

import com.comcast.redirector.core.spring.*;
import com.comcast.redirector.core.spring.configurations.common.BackupBeans;
import com.comcast.redirector.core.spring.configurations.common.CommonBeans;
import com.comcast.redirector.core.spring.configurations.common.ModelBeans;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ContextConfiguration(
    classes = {
        CommonBeans.class, BackupBeans.class, ModelBeans.class,
        IntegrationTestBeans.class, IntegrationTestBackupBeans.class, IntegrationTestApplicationsBeans.class,
        IntegrationTestConfigBeans.class
    }
)
public @interface TestContextConfiguration {
}
