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

package com.comcast.redirector.core.modelupdate.converter;

import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.util.ThreadLocalLogger;
import com.comcast.redirector.core.balancer.serviceprovider.whitelist.WhiteList;
import com.comcast.redirector.ruleengine.model.Model;
import com.comcast.redirector.ruleengine.model.URLRuleModel;
import com.comcast.redirector.ruleengine.repository.NamespacedListRepository;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.Collections;

import static com.comcast.redirector.dataaccess.client.IDataSourceConnector.UTF8_CHARSET;

public class ModelTranslationService {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(ModelTranslationService.class);

    private Serializer xmlSerializer;

    public ModelTranslationService(Serializer xmlSerializer) {
        this.xmlSerializer = xmlSerializer;
    }

    public Model translateFlavorRules(SelectServer source, NamespacedListRepository namespacedLists) {
        String flavorRulesXML = getFlavorRulesXML(source);

        Model model = null;
        try {
            if (StringUtils.isNotBlank(flavorRulesXML)) {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document newDocument = builder.parse(new ByteArrayInputStream(flavorRulesXML.getBytes(UTF8_CHARSET)));
                model = new Model(newDocument, namespacedLists);
                log.info("Creating model from xml: \n{}", flavorRulesXML);
            } else {
                log.error("Trying to init null model");
            }
        } catch (Exception ex) {
            log.error("Exception while creating model from xml: \n{} \n{}", flavorRulesXML, ex);
        }

        return model;
    }

    private String getFlavorRulesXML(SelectServer selectServer) {
        if (selectServer != null) {
            try {
                return xmlSerializer.serialize(selectServer);
            } catch (Exception e) {
                log.error("Failed to serialize selectServer from RedirectorWS ", e);
            }
        }

        return null;
    }

    public URLRuleModel translateUrlRules(URLRules source, NamespacedListRepository namespacedLists) {
        String urlRulesXML = getUrlRulesXML(source);
        URLRuleModel model = null;
        try {
            if (StringUtils.isNotBlank(urlRulesXML)) {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document newDocument = builder.parse(new ByteArrayInputStream(urlRulesXML.getBytes(UTF8_CHARSET)));
                model = new URLRuleModel(newDocument, namespacedLists);
                log.info("Rules created from xml: \n{}", urlRulesXML);
            } else {
                log.error("Trying to init null model");
            }
        } catch (Exception ex) {
            log.error("Exception while creating model from xml: \n{}Root cause: {}", urlRulesXML, ex.getCause().getMessage());
        }

        return model;
    }

    private String getUrlRulesXML(URLRules urlRules) {
        if (urlRules != null) {
            try {
                return xmlSerializer.serialize(urlRules);
            } catch (Exception e) {
                log.error("Failed to serialize URLRules from RedirectorWS ", e);
            }
        }

        return null;
    }

    public WhiteList translateWhitelistedStacks(Whitelisted source) {
        WhiteList whiteList = new WhiteList();
        if (source != null && source.getPaths() != null) {
            whiteList.setPaths(source.getPaths());
        } else {
            whiteList.setPaths(Collections.<String>emptyList());
        }

        log.info("Creating model with whitelisted stacks: {}", String.valueOf(whiteList.getPaths()));

        return whiteList;
    }
}
