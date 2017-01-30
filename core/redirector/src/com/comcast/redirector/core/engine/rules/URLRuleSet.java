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

package com.comcast.redirector.core.engine.rules;

import com.comcast.redirector.ruleengine.model.URLRuleModel;
import com.comcast.redirector.ruleengine.model.UrlParams;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class URLRuleSet extends RuleSet<URLRuleModel> implements IURLRuleSet {
    private String fallbackProtocol;
    private String fallbackUrn;
    private int fallbackIPProtocolVersion;
    private int fallbackPort;

    public URLRuleSet(Builder builder) {
        super(builder.model);
        fallbackProtocol = builder.fallbackProtocol;
        fallbackUrn = builder.fallbackUrn;
        fallbackIPProtocolVersion = builder.fallbackIPProtocolVersion;
        fallbackPort = builder.fallbackPort;
    }

    @Override
    public UrlParams getUrlParams(Map<String, String> context) {
        UrlParams result = null;
        if (isAvailable()) {
            result = (UrlParams) model.execute(context);
        }
        if (result == null) {
            result = new UrlParams();
        }

        fillEmptyFinalUrlParamsByFallbackValues(result);
        return result;
    }

    private void fillEmptyFinalUrlParamsByFallbackValues(UrlParams urlParams) {
        if (StringUtils.isBlank(urlParams.getProtocol())) {
            urlParams.setProtocol(fallbackProtocol);
        }

        if (urlParams.getPort() == null || urlParams.getPort() == 0) {
            urlParams.setPort(fallbackPort);
        }

        if (StringUtils.isBlank(urlParams.getUrn())) {
            urlParams.setUrn(fallbackUrn);
        }

        if (urlParams.getIPProtocolVersion() == null || urlParams.getIPProtocolVersion() == 0) {
            urlParams.setIPProtocolVersion(fallbackIPProtocolVersion);
        }
    }

    public static class Builder {
        private URLRuleModel model;
        private String fallbackProtocol;
        private String fallbackUrn;
        private int fallbackIPProtocolVersion;
        private int fallbackPort;

        public Builder setModel(URLRuleModel model) {
            this.model = model;
            return this;
        }

        public Builder setFallbackProtocol(String fallbackProtocol) {
            this.fallbackProtocol = fallbackProtocol;
            return this;
        }

        public Builder setFallbackUrn(String fallbackUrn) {
            this.fallbackUrn = fallbackUrn;
            return this;
        }

        public Builder setFallbackIPProtocolVersion(int fallbackIPProtocolVersion) {
            this.fallbackIPProtocolVersion = fallbackIPProtocolVersion;
            return this;
        }

        public Builder setFallbackPort(int fallbackPort) {
            this.fallbackPort = fallbackPort;
            return this;
        }

        public URLRuleSet build() {
            return new URLRuleSet(this);
        }
    }
}
