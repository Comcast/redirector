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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package com.comcast.redirector.api.model.distribution;

import com.comcast.redirector.api.model.Server;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class DistributionTest extends Assert {

    @Test
    public void testCreateObject() throws Exception {
        Distribution distribution = new Distribution();
        List<Rule> rules = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Rule rule = new Rule();
            rule.setPercent(i);

            Server serv = new Server();
            serv.setName("name" + i);
            serv.setPath("path" + i);
            serv.setUrl("url" + i);
            rule.setServer(serv);
            rules.add(rule);
        }
        distribution.setRules(rules);
        String result = serializeIt(distribution, true);
        System.out.println(result);
        assertNotNull(result);


    }

    private String serializeIt(Distribution dist, Boolean format) throws Exception {
        JAXBContext context = JAXBContext.newInstance(Distribution.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, format);

        StringWriter result = new StringWriter();
        marshaller.marshal(dist, result);

        return result.toString();
    }
}
