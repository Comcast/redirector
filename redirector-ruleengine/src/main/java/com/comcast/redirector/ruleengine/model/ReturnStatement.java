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

import org.w3c.dom.Element;

import java.net.URL;
import java.util.List;
import java.util.Map;

public class ReturnStatement extends Statement {
    private static final String HOST_PLACEHOLDER = "{host}";

	protected Object value;
    private ReturnStatementType type = ReturnStatementType.PATH_RULE; // default value - Path (Main rule set) rule

	@Override
	public Object execute(Map<String, String> params) {
        if (value instanceof Server) {
            ((Server) value).setReturnStatementType(type);
        }
		return value;
	}

	/**
	 * The value of the return statement is assumed to be either an URL or a partial URL
	 * without the protocol.
	 */
	public void setValue(String value) {
		int pos = 0;
		if ((pos = value.indexOf("://")) != -1) {
			value = value.substring(pos + 3);
		}
		try {
			// check validity of URL
			String temp = "http://" + value; // simulate this as an http url for validity
			new URL(temp);
		}
		catch(Exception e) {
			throw new IllegalArgumentException("Error with return URL: " + value, e);
		}
		this.value = value;
	}

    @Override
    protected void init(Element element) {
        // look for a servergroup if it exists, initialize it
        // if there is no servergroup, look for a Server
        // if there is no server, get the string value and create a server
        List<Element> children = getChildElements(element);
        if (children == null || children.size() == 0) {
            String text = element.getTextContent().trim();
            if (text == null || text.isEmpty()) {
                throw new IllegalStateException("return element has no value");
            }
            if (text.contains(HOST_PLACEHOLDER)) {
                throw new IllegalStateException("placeholder {host} is not allowed for return string");
            }
            Server server = new Server();
            server.setURL(text);
            value = server;
        } else {
            Element child = children.get(0);
            if (child.getTagName().trim().equals(Model.TAG_SERVER_GROUP)) {
                value = model.createLanguageElement(child);
            } else if (child.getTagName().trim().equals(Model.TAG_SERVER)) {
                value = model.createLanguageElement(child);
            } else if (child.getTagName().trim().equals(URLRuleModel.TAG_URL_RULE)) {
                value = model.createLanguageElement(child);
                setType(ReturnStatementType.URL_RULE);
            }
        }
    }

	@Override
	public boolean returnFulfilled() {
		return value != null;
	}

	@Override
	public String toString(int indent, Map<String, String> params) {
		return doSpacing(value.toString(), indent);
	}

    public ReturnStatementType getType() {
        return type;
    }

    public void setType(ReturnStatementType type) {
        this.type = type;
    }
}
