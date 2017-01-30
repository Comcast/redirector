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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.redirector.ruleengine.model.expressions;

import com.comcast.redirector.ruleengine.model.NamespacedList;
import com.comcast.redirector.ruleengine.model.Values;
import org.apache.commons.lang3.StringUtils;


public class RightSide {
    private Values values;
    private NamespacedList namespacedList;

    public Values getValues() {
        return values == null ? new Values() : values;
    }

    public void setValues(Values values) {
        this.values = values;
    }

    public NamespacedList getNamespacedList() {
        return namespacedList == null ? new NamespacedList() : namespacedList;
    }

    public void setNamespacedList(NamespacedList namespacedList) {
        this.namespacedList = namespacedList;
    }

    public String toString(int indent) {
        String valuesStr;
        if (values != null) {
            valuesStr = values.toString();
            if (valuesStr.length() > 20) {
                valuesStr = valuesStr.substring(0, 19) + "...}";
            }
        } else {
            valuesStr = namespacedList.toString();
            if (valuesStr.length() > 20) {
                valuesStr = valuesStr.substring(0, 19) + "...}";
            }
        }
        valuesStr = valuesStr + StringUtils.repeat(' ', indent);
        return valuesStr;
    }
}
