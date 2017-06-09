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

package com.comcast.redirector.common.serializers;

import com.comcast.redirector.api.model.xrestack.HostIPs;
import junit.framework.Assert;
import org.junit.Test;

public class ServiceDiscoveryHostJsonSerializerTest {
    private static final String TEMPLATE_DATA = "{\"name\":\"xreGuide\",\"id\":\"100.200.100.106:10004\",\"address\":\"100.200.100.106\",\"port\":10004,\"sslPort\":null,\"payload\":{\"@class\":\"com.comcast.tvx.cloud.MetaData\",\"workerId\":\"984cdf1d-bfc1-4845-82ba-34c93852dc5b\",\"listenAddress\":\"100.200.100.106\",\"listenPort\":10004,\"serviceName\":\"xreGuide\"{PARAMETERS}},\"registrationTimeUTC\":1426503119389,\"serviceType\":\"DYNAMIC\",\"uriSpec\":null}";
    private static final String TEMPLATE_PARAMETERS = ",\"parameters\":{\"ipv6Address\":{IPV6},\"ipv4Address\":{IPV4}}";
    private static final String IPV4_TEMPLATE = "{IPV4}";
    private static final String IPV6_TEMPLATE = "{IPV6}";
    private static final String PARAMETERS_TEMPLATE = "{PARAMETERS}";
    private static final String IPV4_VALUE = "IPV4";
    private static final String IPV6_VALUE = "IPV6";
    private static final String NULL = "null";

    private ServiceDiscoveryHostJsonSerializer testee = new ServiceDiscoveryHostJsonSerializer();

    @Test
    public void testDeserializeItemWithoutIpv4() throws Exception {
        String input = getPayload(NULL, IPV6_VALUE);

        HostIPs result = testee.deserialize(input);

        Assert.assertNull(result.getIpV4Address());
        Assert.assertEquals(IPV6_VALUE, result.getIpV6Address());
    }

    @Test
    public void testDeserializeItemWithoutIpv6() throws Exception {
        String input = getPayload(IPV4_VALUE, NULL);

        HostIPs result = testee.deserialize(input);

        Assert.assertNull(result.getIpV6Address());
        Assert.assertEquals(IPV4_VALUE, result.getIpV4Address());
    }

    @Test
    public void testDeserializeItemWithoutParameters() throws Exception {
        String input = getPayloadWithoutParameters();

        HostIPs result = testee.deserialize(input);

        Assert.assertNull(result.getIpV6Address());
        Assert.assertNotNull(result.getIpV4Address());
    }

    private String getPayload(String ipv4, String ipv6) {
        return TEMPLATE_DATA.replace(PARAMETERS_TEMPLATE, TEMPLATE_PARAMETERS).replace(IPV4_TEMPLATE, ipv4).replace(IPV6_TEMPLATE, ipv6);
    }

    private String getPayloadWithoutParameters() {
        return TEMPLATE_DATA.replace(PARAMETERS_TEMPLATE, "");
    }
}
