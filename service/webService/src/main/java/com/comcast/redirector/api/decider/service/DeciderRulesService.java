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

package com.comcast.redirector.api.decider.service;

import com.comcast.redirector.api.model.ErrorMessage;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.dataaccess.dao.IListDAO;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Collection;

@Service
public class DeciderRulesService implements IDeciderRulesService {
    @Autowired
    private IListDAO<IfExpression> deciderRulesDAO;

    @Override
    public SelectServer getRules() {
        SelectServer selectServer = new SelectServer();
        selectServer.setItems(deciderRulesDAO.getAll());
        return selectServer;
    }

    @Override
    public IfExpression getRule(String ruleId) {
        return deciderRulesDAO.getById(ruleId);
    }

    @Override
    public Collection<String> getRuleIds() {
        Collection<IfExpression> rules = deciderRulesDAO.getAll();
        return Collections2.transform(rules, new Function<IfExpression, String>() {
            @Override
            public String apply(IfExpression input) {
                return input.getId();
            }
        });
    }

    @Override
    public void deleteRule(String ruleId) {
        deciderRulesDAO.deleteById(ruleId);
    }

    @Override
    public void saveRule(IfExpression rule, String ruleId) {
        try {
            deciderRulesDAO.saveById(rule, ruleId);
        } catch (SerializerException e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage(e.getMessage())).build());
        }
    }
}
