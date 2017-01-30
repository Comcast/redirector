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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */

package com.comcast.xre.redirector.test;

import com.comcast.redirector.ruleengine.RuleEngineInitException;
import com.comcast.redirector.ruleengine.model.Model;
import com.comcast.redirector.core.modelupdate.holder.NamespacedListsHolder;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

/**
 * Test utils class.
 */
public class Utils {

    /**
     * Forms a selectServer statement from given ifStatements.
     * Has no syntax control.
     * @param statements
     * @return
     * @throws RuleEngineInitException
     */
    public static Model buildSelectServer(String... statements) throws RuleEngineInitException {
        StringBuilder sb = new StringBuilder();
        sb.append("<selectServer>");
        for (String statement : statements) {
            sb.append(statement);
        }
        sb.append("</selectServer>");
        return new Model(generateModelFromString(sb.toString()), new NamespacedListsHolder());
    }

    /**
     * Parses a dom Document from String.
     * @param xmlString
     * @return
     */
    public static Document generateModelFromString(String xmlString) {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xmlString.getBytes()));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
