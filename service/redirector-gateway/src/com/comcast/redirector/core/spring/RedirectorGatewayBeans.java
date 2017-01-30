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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.core.spring;

import com.comcast.redirector.PlatformWrapper;
import com.comcast.redirector.core.spring.components.JettyPlatformWrapper;
import com.comcast.redirector.core.spring.components.PlatformWrapperStartBeanPostProcessor;
import com.comcast.redirector.core.spring.configurations.common.BackupBeans;
import com.comcast.redirector.core.spring.configurations.common.CommonBeans;
import com.comcast.redirector.core.spring.configurations.common.FacadeBeans;
import com.comcast.redirector.core.spring.configurations.common.ModelBeans;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonBeans.class, FacadeBeans.class, BackupBeans.class, ModelBeans.class})
@ComponentScan("com.comcast.redirector.core.spring.components")
public class RedirectorGatewayBeans {

    @Bean
    public PlatformWrapper jettyPlatformWrapper () {
        return new JettyPlatformWrapper();
    }

    @Bean
    public BeanPostProcessor platformWrapperStartBeanPostProcessor () {
        return new PlatformWrapperStartBeanPostProcessor();
    }
}
