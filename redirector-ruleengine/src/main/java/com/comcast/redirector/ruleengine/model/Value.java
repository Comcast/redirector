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

import com.comcast.redirector.ruleengine.SimpleValue;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.util.Map;

public class Value extends LanguageElement implements com.comcast.redirector.ruleengine.Value, SimpleValue {
    private static final Logger LOGGER = LoggerFactory.getLogger(Value.class);

    protected String stringValue;
	protected double doubleValue;
	protected boolean numericValue;
	
	@Override
	protected void init(Element element) {
		String text = element.getTextContent().trim();
		if (StringUtils.isEmpty(text)) {
			throw new IllegalStateException("parameter element has no value");
		}
		setValue(text);
	}
	
	public String getValue() {
		return stringValue;
	}
	
	public void setValue(String value) {
		stringValue = value;
		numericValue = false;
        try {
            doubleValue = Double.parseDouble(value);
            numericValue = true;
        } catch (NumberFormatException ex) {
            LOGGER.warn("parameter element value is not a number. found: " + value);
        }
    }

	public String getStringValue(Map<String, String> params) {
		return stringValue;
	}

	public double getNumericValue(Map<String, String> params) {
		return doubleValue;
	}

	public boolean isNumericValue(Map<String, String> params) {
		return numericValue;
	}

	@Override
	public String toString(int indent, Map<String, String> params) {
		return doSpacing(getStringValue(params), indent);
	}
}
