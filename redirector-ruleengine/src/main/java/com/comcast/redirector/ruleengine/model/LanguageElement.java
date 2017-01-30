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
 */
package com.comcast.redirector.ruleengine.model;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class LanguageElement {
	public static final int printSpacing = 2;

	protected AbstractModel model;
	protected String name;
	
	public void init(AbstractModel model, Element element)
	{
		this.model = model;
		name = element.getTagName();
		init(element);
	}
	
	protected abstract void init(Element element);
	
	public List<Element> getChildElements(Element element) {
		List<Element> result = new ArrayList<Element>();
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node instanceof Element) {
				result.add((Element) node);
			}
		}
		return result;
	}
	
	public abstract String toString(int indent, Map<String, String> params);
	
	protected String getName() {
		return name;
	}
	
	protected String doSpacing(String string, int indent) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < indent; i++) {
			sb.append(' ');
		}
		sb.append(string);
		return sb.toString();
	}
}
