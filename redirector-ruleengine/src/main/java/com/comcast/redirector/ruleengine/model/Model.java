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
package com.comcast.redirector.ruleengine.model;

import com.comcast.redirector.ruleengine.RuleEngineInitException;
import com.comcast.redirector.ruleengine.repository.NamespacedListRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Model extends AbstractModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(Model.class);

    private SelectServer selectServer;

    public Model(Document document, NamespacedListRepository namespacedListHolder) throws RuleEngineInitException {
        super(namespacedListHolder);
        try {
            Element root = document.getDocumentElement();
            selectServer = (SelectServer) createLanguageElement(root);
        } catch (Exception e) {
            throw new RuleEngineInitException("Error encountered initializing model. See nested exception:", e);
        }
    }

    @Override
    public List<DistributionServer> getDistribution() {
        List<DistributionServer> serverList = new ArrayList<DistributionServer>();

        if (selectServer.getDistributionStatement() instanceof DistributionStatement) {
            DistributionStatement distributionStatement =
                    ((DistributionStatement) selectServer.getDistributionStatement());

            if (distributionStatement.getDistribution() != null
                    && distributionStatement.getDistribution().getGroups() != null) {

                List<DistributionStatement.Group> groups = distributionStatement.getDistribution().getGroups();
                double percent = 0;
                for (DistributionStatement.Group group : groups) {
                    if (group.getReturnStatement() != null && group.getReturnStatement().value != null) {
                        serverList.add(
                                new DistributionServer((Server) group.getReturnStatement().value,
                                        group.getUpperBound() - percent)
                        );
                        percent = group.getUpperBound();
                    }
                }
            }
        }

        return serverList;
    }

    @Override
    public Object execute(Map<String, String> params) {
        Object result;
        for (IfStatement ifStatement : selectServer.getIfStatements()) {
            result = ifStatement.execute(params);
            if (result != null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\n" + ifStatement.toString(LanguageElement.printSpacing, params));
                }
                return result;
            }
        }
        Statement distributionStatement = selectServer.getDistributionStatement();
        result = distributionStatement.execute(params);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("\nNo if statements were matched.  Using distribution statement for " +
                    "receiverID(" + params.get("receiverId") + "): " + result);
        }

        return result;
    }

    @Override
    public Object executeDefault(Map<String, String> params) {
        DistributionStatement distributionStatement = (DistributionStatement) selectServer.getDistributionStatement();
        if (distributionStatement != null) {
            return distributionStatement.executeDefaultStatement(params);
        }

        return null;
    }
}
