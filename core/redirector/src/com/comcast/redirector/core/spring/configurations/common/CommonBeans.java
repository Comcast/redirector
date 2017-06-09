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

package com.comcast.redirector.core.spring.configurations.common;

import com.comcast.redirector.core.config.ConfigLoader;
import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.core.spring.configurations.base.AbstractCommonBeans;
import com.comcast.xre.common.redirector.selector.xmlimpl.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonBeans extends AbstractCommonBeans {

    @Override
    @Bean
    public ZKConfig config() {
        return ConfigLoader.doParse(Config.class);
    }
}
