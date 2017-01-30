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
package com.comcast.redirector.api.model.appDecider;

import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by alex on 6/24/14.
 */
public class PartnersTest extends Assert {

    @Test
    public void testCreateObject() throws Exception {
        Partners partners = new Partners();
        Set<Partner> partnersList = new LinkedHashSet<>();
        for (int y = 0; y < 3; y++) {
            Set<PartnerProperty> properties = new LinkedHashSet<>();
            for (int i = 0; i < 5; i++) {
                PartnerProperty prop = new PartnerProperty();
                prop.setName("id" + i);
                prop.setValue("xre://test.com:8080/" + i);
                properties.add(prop);
            }
            Partner newPartner = new Partner("test");
            newPartner.setProperties(properties);
            partnersList.add(newPartner);
        }
        partners.setPartners(partnersList);
        String result = serializeIt(partners, true);
        System.out.println(result);
        assertNotNull(result);
    }

    private String serializeIt(Partners partner, Boolean format) throws Exception {
        JAXBContext context = JAXBContext.newInstance(Partners.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, format);

        StringWriter result = new StringWriter();
        marshaller.marshal(partner, result);

        return result.toString();
    }
}
