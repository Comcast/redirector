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

package it.context;

import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.Equals;
import com.comcast.redirector.api.model.IfExpressionType;
import com.comcast.redirector.api.model.InIpRange;
import com.comcast.redirector.api.model.Value;

import java.util.Collections;

public enum Operations {
    EQUALS {
        @Override
        public Expressions getExpression(String left, String right) {
            return new Equals(left, right);
        }
    },
    IN_IP_RANGE_NAMESPACED {
        @Override
        public Expressions getExpression(String left, String right) {
            InIpRange inIpRange = new InIpRange();
            inIpRange.setParam(left);
            inIpRange.setType(IfExpressionType.NAMESPACED_LIST.getType());
            inIpRange.setNamespacedLists(Collections.singletonList(new Value(right)));
            return inIpRange;
        }
    };

    public Expressions getExpression(String left, String right) {
        return null;
    }
}
