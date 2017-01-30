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

package com.comcast.redirector.common.util;

import com.comcast.redirector.api.model.*;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.namespaced.NamespacedListValueForWS;
import com.comcast.redirector.api.model.namespaced.Namespaces;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.api.model.search.NamespacedListSearchResult;
import com.comcast.redirector.api.model.search.RuleEntity;
import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

public class SearchUtilsTest {

    private static final Namespaces ALL_NAMESPACED_LISTS = new Namespaces();
    private static final Map<String, RulesWrapper> ALL_RULES = new LinkedHashMap<>();
    private static final String NS_LIST1_NAME = "list1-name";
    private static final String NS_LIST2_NAME = "list2-name";
    private static final String NS_LIST1_DESCRIPTION = "list1-description";
    private static final String NS_LIST2_DESCRIPTION = "list2-description";
    private static final String IF_EXPRESSION1_ID = "ifExpression1";
    private static final String IF_EXPRESSION2_ID = "ifExpression2";
    private static final String IF_EXPRESSION3_ID = "ifExpression3";
    private static final String SERVICE_NAME_XREGUIDE = "xreGuide";
    private static final String SERVICE_NAME_XREAPP = "xreApp";

    static {

    }

    @BeforeClass
    public static void setUp() {
        final NamespacedList list1 = new NamespacedList();
        list1.setName(NS_LIST1_NAME);
        list1.setDescription(NS_LIST1_DESCRIPTION);
        list1.setValueSet(new LinkedHashSet<NamespacedListValueForWS>() {{
            add(new NamespacedListValueForWS("list1_item1"));
            add(new NamespacedListValueForWS("list1_item2"));
        }});

        final NamespacedList list2 = new NamespacedList();
        list2.setName(NS_LIST2_NAME);
        list2.setDescription(NS_LIST2_DESCRIPTION);
        list2.setValueSet(new LinkedHashSet<NamespacedListValueForWS>() {{
            add(new NamespacedListValueForWS("list2_item1"));
            add(new NamespacedListValueForWS("list2_item2"));
            add(new NamespacedListValueForWS("list2_item3"));
        }});

        ALL_NAMESPACED_LISTS.setNamespaces(new LinkedList<NamespacedList>() {{
            add(list1);
            add(list2);
        }});

        SelectServer xreGuideSelectServer = new SelectServer();
        SelectServer xreAppSelectServer = new SelectServer();

        final Contains simpleContainsExpression = new Contains();
        simpleContainsExpression.setParam("parameterName");
        simpleContainsExpression.setNamespacedLists(new ArrayList<Value>() {{
            add(new Value("list1-name"));
        }});

        final Contains simpleContainsExpression2 = new Contains();
        simpleContainsExpression2.setParam("parameterName");
        simpleContainsExpression2.setNamespacedLists(new ArrayList<Value>() {{
            add(new Value("list2-name"));
        }});

        final AndExpression andExpression = new AndExpression();
        final Equals equalsExpression = new Equals();
        equalsExpression.setParam("someParameter");
        equalsExpression.setValue("someValue");

        andExpression.setItems(new ArrayList<Expressions>() {{
            add(equalsExpression);
            add(simpleContainsExpression);
        }});

        IfExpression ifExpression1 = new IfExpression();
        ifExpression1.setId(IF_EXPRESSION1_ID);
        ifExpression1.setItems(new ArrayList<Expressions>() {{
            add(andExpression);
        }});

        IfExpression ifExpression2 = new IfExpression();
        ifExpression2.setId(IF_EXPRESSION2_ID);
        ifExpression2.setItems(new ArrayList<Expressions>() {{
            add(simpleContainsExpression2);
        }});

        xreGuideSelectServer.addCondition(ifExpression1);
        xreGuideSelectServer.addCondition(ifExpression2);

        ALL_RULES.put(SERVICE_NAME_XREGUIDE, new RulesWrapper(EntityType.RULE.name(), xreGuideSelectServer));
        ALL_RULES.get(SERVICE_NAME_XREGUIDE).addRules(EntityType.TEMPLATE_RULE.name(), xreGuideSelectServer.getItems());
        ALL_RULES.get(SERVICE_NAME_XREGUIDE).addRules(EntityType.URL_RULE.name(), xreGuideSelectServer.getItems());
        ALL_RULES.get(SERVICE_NAME_XREGUIDE).addRules(EntityType.TEMPLATE_URL_RULE.name(), xreGuideSelectServer.getItems());
        ALL_RULES.get(SERVICE_NAME_XREGUIDE).addRules(EntityType.DECIDER_RULE.name(), xreGuideSelectServer.getItems());

        IfExpression ifExpression3 = new IfExpression();
        ifExpression3.setId(IF_EXPRESSION3_ID);
        ifExpression3.setItems(new ArrayList<Expressions>() {{
            add(simpleContainsExpression2);
        }});

        xreAppSelectServer.addCondition(ifExpression3);

        ALL_RULES.put(SERVICE_NAME_XREAPP, new RulesWrapper(EntityType.RULE.name(), xreAppSelectServer));
        ALL_RULES.get(SERVICE_NAME_XREAPP).addRules(EntityType.TEMPLATE_RULE.name(), xreAppSelectServer.getItems());
        ALL_RULES.get(SERVICE_NAME_XREAPP).addRules(EntityType.URL_RULE.name(), xreAppSelectServer.getItems());
        ALL_RULES.get(SERVICE_NAME_XREAPP).addRules(EntityType.TEMPLATE_URL_RULE.name(), xreAppSelectServer.getItems());
        ALL_RULES.get(SERVICE_NAME_XREAPP).addRules(EntityType.DECIDER_RULE.name(), xreAppSelectServer.getItems());
    }

    @Test
    public void testSearchNamespacedLists() {
        NamespacedListValueForWS searchItem1 = new NamespacedListValueForWS("list");
        NamespacedListSearchResult searchResult1 = NamespacedListUtils.searchNamespacedLists(searchItem1, ALL_NAMESPACED_LISTS, ALL_RULES);
        Assert.assertEquals(searchItem1.getValue(), searchResult1.getSearchItem());
        Assert.assertEquals(2, searchResult1.getNamespacedLists().size());

        Assert.assertEquals(NS_LIST1_NAME, searchResult1.getNamespacedLists().get(0).getName());
        Assert.assertEquals(NS_LIST1_DESCRIPTION, searchResult1.getNamespacedLists().get(0).getDescription());
        // validate Flavor rules
        Assert.assertEquals(1, searchResult1.getNamespacedLists().get(0).getDependingFlavorRules().size());
        RuleEntity[] dependingRules = searchResult1.getNamespacedLists().get(0).getDependingFlavorRules().toArray(new RuleEntity[0]);
        Assert.assertEquals(IF_EXPRESSION1_ID, dependingRules[0].getName());
        Assert.assertEquals(SERVICE_NAME_XREGUIDE, dependingRules[0].getServiceName());
        // validate Template Flavor rules
        Assert.assertEquals(1, searchResult1.getNamespacedLists().get(0).getDependingTemplateFlavorRules().size());
        RuleEntity[] dependingTemplateRules = searchResult1.getNamespacedLists().get(0).getDependingTemplateFlavorRules().toArray(new RuleEntity[0]);
        Assert.assertEquals(IF_EXPRESSION1_ID, dependingTemplateRules[0].getName());
        Assert.assertEquals(SERVICE_NAME_XREGUIDE, dependingTemplateRules[0].getServiceName());
        // validate Url rules
        Assert.assertEquals(1, searchResult1.getNamespacedLists().get(0).getDependingUrlRules().size());
        RuleEntity[] dependingUrlRules = searchResult1.getNamespacedLists().get(0).getDependingUrlRules().toArray(new RuleEntity[0]);
        Assert.assertEquals(IF_EXPRESSION1_ID, dependingUrlRules[0].getName());
        Assert.assertEquals(SERVICE_NAME_XREGUIDE, dependingUrlRules[0].getServiceName());
        // validate Template Url rules
        Assert.assertEquals(1, searchResult1.getNamespacedLists().get(0).getDependingTemplateUrlRules().size());
        RuleEntity[] dependingTemplateUrlRules = searchResult1.getNamespacedLists().get(0).getDependingTemplateUrlRules().toArray(new RuleEntity[0]);
        Assert.assertEquals(IF_EXPRESSION1_ID, dependingTemplateUrlRules[0].getName());
        Assert.assertEquals(SERVICE_NAME_XREGUIDE, dependingTemplateUrlRules[0].getServiceName());
        // validate Decider rules
        Assert.assertEquals(1, searchResult1.getNamespacedLists().get(0).getDependingDeciderRules().size());
        RuleEntity[] dependingDeciderRules = searchResult1.getNamespacedLists().get(0).getDependingDeciderRules().toArray(new RuleEntity[0]);
        Assert.assertEquals(IF_EXPRESSION1_ID, dependingDeciderRules[0].getName());
        Assert.assertEquals(SERVICE_NAME_XREGUIDE, dependingDeciderRules[0].getServiceName());

        Assert.assertEquals(NS_LIST2_NAME, searchResult1.getNamespacedLists().get(1).getName());
        Assert.assertEquals(NS_LIST2_DESCRIPTION, searchResult1.getNamespacedLists().get(1).getDescription());
        // validate Flavor rules
        Assert.assertEquals(2, searchResult1.getNamespacedLists().get(1).getDependingFlavorRules().size());
        dependingRules = searchResult1.getNamespacedLists().get(1).getDependingFlavorRules().toArray(new RuleEntity[0]);
        Assert.assertEquals(IF_EXPRESSION2_ID, dependingRules[0].getName());
        Assert.assertEquals(SERVICE_NAME_XREGUIDE, dependingRules[0].getServiceName());
        Assert.assertEquals(IF_EXPRESSION3_ID, dependingRules[1].getName());
        Assert.assertEquals(SERVICE_NAME_XREAPP, dependingRules[1].getServiceName());
        // validate Template Flavor rules
        Assert.assertEquals(2, searchResult1.getNamespacedLists().get(1).getDependingTemplateFlavorRules().size());
        dependingTemplateRules = searchResult1.getNamespacedLists().get(1).getDependingTemplateFlavorRules().toArray(new RuleEntity[0]);
        Assert.assertEquals(IF_EXPRESSION2_ID, dependingTemplateRules[0].getName());
        Assert.assertEquals(SERVICE_NAME_XREGUIDE, dependingTemplateRules[0].getServiceName());
        Assert.assertEquals(IF_EXPRESSION3_ID, dependingTemplateRules[1].getName());
        Assert.assertEquals(SERVICE_NAME_XREAPP, dependingTemplateRules[1].getServiceName());
        // validate Url rules
        Assert.assertEquals(2, searchResult1.getNamespacedLists().get(1).getDependingUrlRules().size());
        dependingUrlRules = searchResult1.getNamespacedLists().get(1).getDependingUrlRules().toArray(new RuleEntity[0]);
        Assert.assertEquals(IF_EXPRESSION2_ID, dependingUrlRules[0].getName());
        Assert.assertEquals(SERVICE_NAME_XREGUIDE, dependingUrlRules[0].getServiceName());
        Assert.assertEquals(IF_EXPRESSION3_ID, dependingUrlRules[1].getName());
        Assert.assertEquals(SERVICE_NAME_XREAPP, dependingUrlRules[1].getServiceName());
        // validate Template Url rules
        Assert.assertEquals(2, searchResult1.getNamespacedLists().get(1).getDependingTemplateUrlRules().size());
        dependingTemplateUrlRules = searchResult1.getNamespacedLists().get(1).getDependingTemplateUrlRules().toArray(new RuleEntity[0]);
        Assert.assertEquals(IF_EXPRESSION2_ID, dependingTemplateUrlRules[0].getName());
        Assert.assertEquals(SERVICE_NAME_XREGUIDE, dependingTemplateUrlRules[0].getServiceName());
        Assert.assertEquals(IF_EXPRESSION3_ID, dependingTemplateUrlRules[1].getName());
        Assert.assertEquals(SERVICE_NAME_XREAPP, dependingTemplateUrlRules[1].getServiceName());
        // validate Decider rules
        Assert.assertEquals(2, searchResult1.getNamespacedLists().get(1).getDependingDeciderRules().size());
        dependingDeciderRules = searchResult1.getNamespacedLists().get(1).getDependingDeciderRules().toArray(new RuleEntity[0]);
        Assert.assertEquals(IF_EXPRESSION2_ID, dependingDeciderRules[0].getName());
        Assert.assertEquals(SERVICE_NAME_XREGUIDE, dependingDeciderRules[0].getServiceName());
        Assert.assertEquals(IF_EXPRESSION3_ID, dependingDeciderRules[1].getName());
        Assert.assertEquals(SERVICE_NAME_XREAPP, dependingDeciderRules[1].getServiceName());

        NamespacedListValueForWS searchItem2 = new NamespacedListValueForWS("list1");
        NamespacedListSearchResult searchResult2 = NamespacedListUtils.searchNamespacedLists(searchItem2, ALL_NAMESPACED_LISTS, ALL_RULES);
        Assert.assertEquals(searchItem2.getValue(), searchResult2.getSearchItem());
        Assert.assertEquals(1, searchResult2.getNamespacedLists().size());
        Assert.assertEquals(NS_LIST1_NAME, searchResult2.getNamespacedLists().get(0).getName());

        NamespacedListValueForWS searchItem3 = new NamespacedListValueForWS("list2_item2");
        NamespacedListSearchResult searchResult3 = NamespacedListUtils.searchNamespacedLists(searchItem3, ALL_NAMESPACED_LISTS, ALL_RULES);
        Assert.assertEquals(searchItem3.getValue(), searchResult3.getSearchItem());
        Assert.assertEquals(1, searchResult3.getNamespacedLists().size());
        Assert.assertEquals(NS_LIST2_NAME, searchResult3.getNamespacedLists().get(0).getName());
    }
}
