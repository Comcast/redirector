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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package com.comcast.redirector.ruleengine.model;

import com.comcast.redirector.ruleengine.RuleEngineInitException;
import com.comcast.redirector.ruleengine.model.expressions.Percent;
import com.comcast.redirector.ruleengine.repository.NamespacedListRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;

public class URLRuleModel extends AbstractModel {

    private static Logger log = LoggerFactory.getLogger(URLRuleModel.class);

    public final static String TAG_URL_RULE = "urlRule";
    final static String TAG_URL_RULES = "urlRules";
    final static String TAG_DEFAULT_SECTION = "default";
    final static String TAG_PERCENT = "percent";

    private URLRules urlRules;

    static {
        TAG_MAP.put(TAG_URL_RULE, UrlParams.class);
        TAG_MAP.put(TAG_URL_RULES, URLRules.class);
        TAG_MAP.put(TAG_DEFAULT_SECTION, UrlRuleDefaultStatement.class);
        TAG_MAP.put(TAG_PERCENT, Percent.class);
    }

    public URLRuleModel(Document document, NamespacedListRepository namespacedListHolder) throws RuleEngineInitException {
        super(namespacedListHolder);
        try {
            Element root = document.getDocumentElement();
            urlRules = (URLRules) createLanguageElement(root);
        } catch (Exception e) {
            throw new RuleEngineInitException("Error encountered initializing model. See nested exception:", e);
        }
    }

    @Override
    public Object execute(Map<String, String> params) {
        UrlParams finalResult = new UrlParams();
        for (IfStatement ifStatement : urlRules.getIfStatements()) {
            UrlParams ifResult;
            ifResult = (UrlParams) ifStatement.execute(params);
            if (ifResult != null) {
                if (log.isDebugEnabled()) {
                    log.debug("\n" + ifStatement.toString(LanguageElement.printSpacing, params));
                }

                finalResult = mergeUrlParams(ifResult, finalResult);
                finalResult.addAppliedRuleName(ifStatement.getId());
                if (finalResult.allItemsFilled()) {
                    return finalResult;
                }
            }
        }

        //otherwise get absent data from default section
        if (urlRules.getDefaultStatement() != null) {
            UrlParams defaultStatement = (UrlParams) urlRules.getDefaultStatement().execute(params);
            finalResult = mergeUrlParams(defaultStatement, finalResult);
            finalResult.addAppliedRuleName("Default");
        }
        return finalResult;
    }

    private UrlParams mergeUrlParams(UrlParams from, UrlParams to) {
        if (StringUtils.isBlank(to.getProtocol()) && StringUtils.isNotBlank(from.getProtocol())) {
            to.setProtocol(from.getProtocol());
        }

        if (StringUtils.isBlank(to.getUrn()) && StringUtils.isNotBlank(from.getUrn())) {
            to.setUrn(from.getUrn());
        }

        if ((to.getPort() == null || to.getPort() == 0) && from.getPort() != null) {
            to.setPort(from.getPort());
        }

        if ((to.getIPProtocolVersion() == null || to.getIPProtocolVersion() == 0)
                && from.getIPProtocolVersion() != null) {
            to.setIPProtocolVersion(from.getIPProtocolVersion());
        }

        return to;
    }

    @Override
    public List<DistributionServer> getDistribution() {
        return null;
    }

    @Override
    public Object executeDefault(Map<String, String> params) {
        return urlRules.getDefaultStatement().execute(params);
    }
}
