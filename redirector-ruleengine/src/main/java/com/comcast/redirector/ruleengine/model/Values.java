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
import org.w3c.dom.NodeList;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class Values extends LanguageElement {
	private final static String DELIMETER = "delimeter";

	private Set<String> values = new HashSet<String>();
	private String valueDelimeter = ",";

	@Override
	protected void init(Element element) {
		NodeList valueNodes = element.getElementsByTagName(Model.TAG_VALUE);
		
		// if value tags exist, use them
		if (valueNodes != null && valueNodes.getLength() > 0) {
			for (int i = 0; i < valueNodes.getLength(); i++) {
				values.add(valueNodes.item(i).getTextContent());
			}
		}
		else {
			// check for delimeter
			String delim = element.getAttribute(DELIMETER);
			if (delim != null && delim.length() > 0)
			{
				valueDelimeter = delim;
			}

			// parse values 
			String text = element.getTextContent().trim();
			if (text == null || text.length() == 0) {
				throw new IllegalStateException("values element has no value");
			}
			StringTokenizer st = new StringTokenizer(text, valueDelimeter);
			while (st.hasMoreTokens()) {
				String temp = st.nextToken().trim();
				if (temp.length() > 0) {
					values.add(temp);
				}
			}
		}
	}
	
	public boolean contains(String s)
	{
		return values.contains(s);
	}

	public Set<String> getValues() {
		return values;
	}

	public void setValues(Set<String> values) {
		this.values = values;
	}

	@Override
	public String toString(int indent, Map<String, String> params) {
		String valuesStr = values.toString();
		if (valuesStr.length() > 20) {
			valuesStr = valuesStr.substring(0, 19) + "...}";
		}
		return doSpacing(valuesStr, indent);
	}

	

}
