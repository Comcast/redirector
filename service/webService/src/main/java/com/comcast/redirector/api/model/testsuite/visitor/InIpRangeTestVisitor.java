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

package com.comcast.redirector.api.model.testsuite.visitor;

import com.comcast.redirector.api.model.InIpRange;
import com.comcast.redirector.api.model.Value;
import com.comcast.redirector.common.util.CIDR;
import java.util.Collections;
import java.util.List;

@TestSuiteVisitor(forClass = InIpRange.class)
public class InIpRangeTestVisitor extends ContainsBaseExpressionVisitor<InIpRange> {
    @Override
    protected List<Value> getValuesForNegation() {
        return Collections.singletonList(new Value("0.0.0.1"));
    }

    @Override
    protected Value obtainParameterValue(String ipAddressString) {
        String finalIp;
        finalIp = CIDR.isValidFormatCIDR(ipAddressString) ? new CIDR(ipAddressString).getEndAddress() : ipAddressString;
        return new Value(finalIp);
    }
}
