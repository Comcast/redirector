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

import com.comcast.redirector.ruleengine.SimpleValue;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import java.util.Map;

public class Pattern extends LanguageElement implements SimpleValue {
	private String pattern;

	@Override
	protected void init(Element element) {
		String text = element.getTextContent().trim();
		if (StringUtils.isEmpty(text)) {
			throw new IllegalStateException("pattern element has no value");
		}
		setValue(text);
	}

	public String getValue() {
		return pattern;
	}

	public void setValue(String value) {
		pattern = value;
	}

	@Override
	public String toString(int indent, Map<String, String> params) {
		return doSpacing(getValue(), indent);
	}
}
