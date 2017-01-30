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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.common.serializers;

import com.comcast.redirector.api.model.xrestack.HostIPs;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class ServiceDiscoveryHostJsonSerializer implements ServiceDiscoveryHostDeserializer {
    private static final String HOST_PARAMETERS_NAME = "parameters";
    private static final String HOST_PAYLOAD_NAME = "payload";
    public static final String IPV4_ADDRESS_FIELD_NAME = "ipv4Address";
    public static final String IPV6_ADDRESS_FIELD_NAME = "ipv6Address";
    public static final String WEIGHT_FIELD_NAME = "weight";
    public static final String OLD_ADDRESS_FIELD_NAME = "address";

    @Override
    public HostIPs deserialize(String data) throws SerializerException {
        try {
            HostIPs hostIPs;
            JSONObject object = new JSONObject(data);
            if (hasPath(object,
                    HOST_PAYLOAD_NAME,
                    HOST_PARAMETERS_NAME,
                    IPV6_ADDRESS_FIELD_NAME)) {
                JSONObject parameters = object.getJSONObject(HOST_PAYLOAD_NAME).getJSONObject(HOST_PARAMETERS_NAME);
                hostIPs = new HostIPs(
                        getStringValueOrNull(parameters, IPV4_ADDRESS_FIELD_NAME),
                        getStringValueOrNull(parameters, IPV6_ADDRESS_FIELD_NAME),
                        getStringValueOrNull(parameters, WEIGHT_FIELD_NAME));
            } else {
                String weight = null;
                if (hasPath(object,
                    HOST_PAYLOAD_NAME,
                    HOST_PARAMETERS_NAME,
                    "weight")) {
                    JSONObject parameters = object.getJSONObject(HOST_PAYLOAD_NAME).getJSONObject(HOST_PARAMETERS_NAME);
                    weight = getStringValueOrNull(parameters, WEIGHT_FIELD_NAME);
                }

                hostIPs = new HostIPs(object.getString(OLD_ADDRESS_FIELD_NAME), null, weight);
            }

            return hostIPs;
        } catch (JSONException e) {
            throw new SerializerException("failed to parse host " + data, e);
        }
    }

    private static String getStringValueOrNull(JSONObject parent, String key) throws JSONException {
        return parent.isNull(key) ? null : parent.getString(key);
    }

    private boolean hasPath (JSONObject object, String ... path) {
        JSONObject temp = object;
        try {
            for (String s : path) {
                if (s.equals(path[path.length -1])){
                    temp.getString(s);
                } else {
                    temp = temp.getJSONObject(s);
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
