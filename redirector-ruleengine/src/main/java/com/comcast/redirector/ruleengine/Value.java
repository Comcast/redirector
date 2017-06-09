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
package com.comcast.redirector.ruleengine;

import java.util.Map;


/**
 * @author rseamon
 * An IValue represents a value that can be compared using one of the <link>RelationalExpression</link>s.
 */
public interface Value {
	String getStringValue(Map<String, String> params);

	double getNumericValue(Map<String, String> params);

	boolean isNumericValue(Map<String, String> params);

	String toString(int indent, Map<String, String> params);
}
