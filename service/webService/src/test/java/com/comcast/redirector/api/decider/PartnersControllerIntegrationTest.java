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
 * @author Alexey Smolenskiy (asmolenskiy@productengine.com)
 */

package com.comcast.redirector.api.decider;

import com.comcast.redirector.api.model.appDecider.Partner;
import com.comcast.redirector.api.model.appDecider.PartnerProperty;
import com.comcast.redirector.api.model.appDecider.Partners;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.api.redirector.helpers.ServiceHelper;
import com.comcast.redirector.common.DeciderConstants;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.matchers.JUnitMatchers.containsString;
import static org.junit.matchers.JUnitMatchers.hasItems;

public class PartnersControllerIntegrationTest {

    private static final Partners PARTNERS;
    private static final String JSON_PARTNER_1;
    private static final String JSON_PARTNER_2;
    private static final String JSON_PARTNERS;
    private static final String XML_PARTNER_1;
    private static final String XML_PARTNER_2;
    private static final String XML_PARTNERS;

    private static final String DECIDER_PARTNERS_PATH = DeciderConstants.DECIDER_PARTNERS_PATH;


    static {
        PARTNERS = createDefaultPartners();
        // JSON_PARTNERS and XML_PARTNERS are the same as PARTNERS
        JSON_PARTNER_1 = "{\"id\":\"p1\",\"properties\":{\"property\":[{\"name\":\"prop2\",\"value\":\"val2\"},{\"name\":\"prop1\",\"value\":\"val1\"}]}}";
        JSON_PARTNER_2 = "{\"id\":\"p2\",\"properties\":{\"property\":[{\"name\":\"prop3\",\"value\":\"val3\"}]}}";
        JSON_PARTNERS = "{\"partner\":[" + JSON_PARTNER_2 + "," + JSON_PARTNER_1 + "]}";

        XML_PARTNER_1 = "<partner id=\"p1\"><properties><property name=\"prop1\">val1</property><property name=\"prop2\">val2</property></properties></partner>";
        XML_PARTNER_2 = "<partner id=\"p2\"><properties><property name=\"prop3\">val3</property></properties></partner>";
        XML_PARTNERS = "<partners>" + XML_PARTNER_2 + XML_PARTNER_1 + "</partners>";
    }

    @After
    public void cleanUp() throws Exception {
        // clean up partners data
        ServiceHelper.post(getWebTarget_Post(), createEmptyPartners(), MediaType.APPLICATION_JSON);
     }

    /**
     * test check for {com.comcast.redirector.api.decider.PartnersController#savePartners(Partners, javax.ws.rs.core.UriInfo)}
     * test check for {com.comcast.redirector.api.decider.PartnersController#getAllPartners(javax.ws.rs.core.UriInfo)}
     */
    @Test
    public void testSaveGetAllPartners_JsonObject() {
        // post our partners
        Partners responseEntityObject = ServiceHelper.post(getWebTarget_Post(), PARTNERS, MediaType.APPLICATION_JSON);
        // validate response
        try {
            validatePartnersEquals(PARTNERS, responseEntityObject);
        } catch (AssertionError ae) {
            Assert.fail(ae.getMessage());
        }

        // check get response
        responseEntityObject = ServiceHelper.get(getWebTarget_GetAll(), MediaType.APPLICATION_JSON, Partners.class);
        // validate response
        try {
            validatePartnersEquals(PARTNERS, responseEntityObject);
        } catch (AssertionError ae) {
            Assert.fail(ae.getMessage());
        }
    }

    /**
     * test check for {com.comcast.redirector.api.decider.PartnersController#savePartners(Partners, javax.ws.rs.core.UriInfo)}
     * test check for {com.comcast.redirector.api.decider.PartnersController#getAllPartners(javax.ws.rs.core.UriInfo)}
     */
    @Test
    public void testSaveGetAllPartners_XmlObject() {
        // post our partners
        Partners responseEntityObject = ServiceHelper.post(getWebTarget_Post(), PARTNERS, MediaType.APPLICATION_XML);
        // validate response
        try {
            validatePartnersEquals(PARTNERS, responseEntityObject);
        } catch (AssertionError ae) {
            Assert.fail(ae.getMessage());
        }

        // check get response
        responseEntityObject = ServiceHelper.get(getWebTarget_GetAll(), MediaType.APPLICATION_XML, Partners.class);
        // validate response
        try {
            validatePartnersEquals(PARTNERS, responseEntityObject);
        } catch (AssertionError ae) {
            Assert.fail(ae.getMessage());
        }
    }

    /**
     * test check for {com.comcast.redirector.api.decider.PartnersController#savePartners(Partners, javax.ws.rs.core.UriInfo)}
     * test check for {com.comcast.redirector.api.decider.PartnersController#getAllPartners(javax.ws.rs.core.UriInfo)}
     * MediaType - JSON string format to check corrected structure
     */
    @Test
    public void testSaveAllPartners_JsonString() {
        // post our JSON partners
        ServiceHelper.post(getWebTarget_Post(), JSON_PARTNERS, MediaType.APPLICATION_JSON);

        // check get response
        Partners responseEntityObject =
                ServiceHelper.get(getWebTarget_GetAll(), MediaType.APPLICATION_JSON, Partners.class);
        // validate response
        try {
            validatePartnersEquals(PARTNERS, responseEntityObject);
        } catch (AssertionError ae) {
            Assert.fail(ae.getMessage());
        }
    }

    /**
     * test check for {com.comcast.redirector.api.decider.PartnersController#savePartners(Partners, javax.ws.rs.core.UriInfo)}
     * test check for {com.comcast.redirector.api.decider.PartnersController#getAllPartners(javax.ws.rs.core.UriInfo)}
     * MediaType - XML string format to check corrected structure
     */
    @Test
    public void testSaveAllPartners_XmlString() {
        // post our XML partners
        ServiceHelper.post(getWebTarget_Post(), XML_PARTNERS, MediaType.APPLICATION_XML);

        // check get response
        Partners responseEntityObject =
                ServiceHelper.get(getWebTarget_GetAll(), MediaType.APPLICATION_XML, Partners.class);
        // validate response
        try {
            validatePartnersEquals(PARTNERS, responseEntityObject);
        } catch (AssertionError ae) {
            Assert.fail(ae.getMessage());
        }
    }

    /**
     * test check for {com.comcast.redirector.api.decider.PartnersController#getAllPartners(javax.ws.rs.core.UriInfo)}
     * MediaType - JSON string format to check corrected structure
     */
    @Test
    public void testGetAllPartners_JsonString() throws IOException {
        final String JSON_Partners = "{\"partner\":[{\"id\":\"p1\",\"properties\":{\"property\":[{\"name\":\"prop1\",\"value\":\"val1\"}]}}]}";

        // post our partners
        ServiceHelper.post(getWebTarget_Post(), JSON_Partners, MediaType.APPLICATION_JSON);

        // check get response
        String responseEntityObject =
                ServiceHelper.get(getWebTarget_GetAll(), MediaType.APPLICATION_JSON, String.class);
        // validate response
        Assert.assertEquals(JSON_Partners, responseEntityObject);
    }

    /**
     * test check for {com.comcast.redirector.api.decider.PartnersController#getAllPartners(javax.ws.rs.core.UriInfo)}
     * MediaType - XML string format to check corrected structure
     */
    @Test
    public void testGetAllPartners_XmlString() {
        final String XML_Partners = "<partners><partner id=\"p1\"><properties><property name=\"prop1\">val1</property></properties></partner></partners>";

        // post our partners
        ServiceHelper.post(getWebTarget_Post(), XML_Partners, MediaType.APPLICATION_XML);

        // check get response
        String responseEntityObject =
                ServiceHelper.get(getWebTarget_GetAll(), MediaType.APPLICATION_XML, String.class);
        // validate response
        Assert.assertThat(responseEntityObject, containsString(XML_Partners));
    }

    /**
     * test check for {com.comcast.redirector.api.decider.PartnersController#addOrUpdatePartner(Partner, javax.ws.rs.core.UriInfo)}
     */
    @Test
    public void testAddParticularPartner_JsonObject() {
        // post our partners
        ServiceHelper.post(getWebTarget_Post(), PARTNERS, MediaType.APPLICATION_JSON);

        // add new Partner
        Partner newPartner = createPartner("partner10", createPartnerProperty("prop10", "value10"));
        Partner responsePartner = ServiceHelper.put(getWebTarget_Put(), newPartner, MediaType.APPLICATION_JSON);
        // validate response
        try {
            validatePartnerEquals(newPartner, responsePartner);
        } catch (AssertionError ae) {
            Assert.fail(ae.getMessage());
        }

        // check get response
        Partners responseEntityObject = ServiceHelper.get(getWebTarget_GetAll(), MediaType.APPLICATION_JSON, Partners.class);
        Assert.assertEquals(PARTNERS.getPartners().size() + 1, responseEntityObject.getPartners().size());
        Partner addedPartner = getPartnerById(newPartner.getId(), responseEntityObject);
        // validate added partner
        try {
            validatePartnerEquals(newPartner, addedPartner);
        } catch (AssertionError ae) {
            Assert.fail(ae.getMessage());
        }
    }

    /**
     * test check for {com.comcast.redirector.api.decider.PartnersController#addOrUpdatePartner(Partner, javax.ws.rs.core.UriInfo)}
     */
    @Test
    public void testAddParticularPartner_XmlObject() {
        // post our partners
        ServiceHelper.post(getWebTarget_Post(), PARTNERS, MediaType.APPLICATION_XML);

        // add new Partner
        Partner newPartner = createPartner("partner10", createPartnerProperty("prop10", "value10"));
        Partner responsePartner = ServiceHelper.put(getWebTarget_Put(), newPartner, MediaType.APPLICATION_XML);
        // validate response
        try {
            validatePartnerEquals(newPartner, responsePartner);
        } catch (AssertionError ae) {
            Assert.fail(ae.getMessage());
        }

        // check get response
        Partners responseEntityObject = ServiceHelper.get(getWebTarget_GetAll(), MediaType.APPLICATION_XML, Partners.class);
        Assert.assertEquals(PARTNERS.getPartners().size() + 1, responseEntityObject.getPartners().size());
        Partner addedPartner = getPartnerById(newPartner.getId(), responseEntityObject);
        // validate added partner
        try {
            validatePartnerEquals(newPartner, addedPartner);
        } catch (AssertionError ae) {
            Assert.fail(ae.getMessage());
        }
    }

    /**
     * test check for {com.comcast.redirector.api.decider.PartnersController#addOrUpdatePartner(Partner, javax.ws.rs.core.UriInfo)}
     */
    @Test
    public void testAddParticularPartner_JsonString() {
        final String JSON_Partner = "{\"id\":\"partner10\",\"properties\":{\"property\":[{\"name\":\"prop10\",\"value\":\"value10\"}]}}";
        Partner newPartner = createPartner("partner10", createPartnerProperty("prop10", "value10"));

        // post our partners
        ServiceHelper.post(getWebTarget_Post(), PARTNERS, MediaType.APPLICATION_JSON);

        // add new Partner
        String responsePartner = ServiceHelper.put(getWebTarget_Put(), JSON_Partner, MediaType.APPLICATION_JSON);
        // validate response
        Assert.assertEquals(JSON_Partner, responsePartner);

        // check get response
        Partners responseEntityObject = ServiceHelper.get(getWebTarget_GetAll(), MediaType.APPLICATION_JSON, Partners.class);
        Assert.assertEquals(PARTNERS.getPartners().size() + 1, responseEntityObject.getPartners().size());
        Partner addedPartner = getPartnerById(newPartner.getId(), responseEntityObject);
        // validate added partner
        try {
            validatePartnerEquals(newPartner, addedPartner);
        } catch (AssertionError ae) {
            Assert.fail(ae.getMessage());
        }
    }

    /**
     * test check for {com.comcast.redirector.api.decider.PartnersController#addOrUpdatePartner(Partner, javax.ws.rs.core.UriInfo)}
     */
    @Test
    public void testAddParticularPartner_XmlString() {
        final String XML_Partner = "<partner id=\"partner10\"><properties><property name=\"prop10\">value10</property></properties></partner>";
        Partner newPartner = createPartner("partner10", createPartnerProperty("prop10", "value10"));

        // post our partners
        ServiceHelper.post(getWebTarget_Post(), PARTNERS, MediaType.APPLICATION_XML);

        // add new Partner
        String responsePartner = ServiceHelper.put(getWebTarget_Put(), XML_Partner, MediaType.APPLICATION_XML);
        // validate response
        Assert.assertThat(responsePartner, containsString(XML_Partner));

        // check get response
        Partners responseEntityObject = ServiceHelper.get(getWebTarget_GetAll(), MediaType.APPLICATION_XML, Partners.class);
        Assert.assertEquals(PARTNERS.getPartners().size() + 1, responseEntityObject.getPartners().size());
        Partner addedPartner = getPartnerById(newPartner.getId(), responseEntityObject);
        // validate added partner
        try {
            validatePartnerEquals(newPartner, addedPartner);
        } catch (AssertionError ae) {
            Assert.fail(ae.getMessage());
        }
    }

    /**
     * test check for {com.comcast.redirector.api.decider.PartnersController#addOrUpdatePartner(Partner, javax.ws.rs.core.UriInfo)}
     */
    @Test
    public void testUpdateParticularPartner_JsonObject() {
        // post our partners
        ServiceHelper.post(getWebTarget_Post(), PARTNERS, MediaType.APPLICATION_JSON);

        // update "p1" Partner
        Partner partnerForUpdate = createPartner("p1", createPartnerProperty("prop10", "value10"));
        Partner responsePartner = ServiceHelper.put(getWebTarget_Put(), partnerForUpdate, MediaType.APPLICATION_JSON);
        // validate response
        try {
            validatePartnerEquals(partnerForUpdate, responsePartner);
        } catch (AssertionError ae) {
            Assert.fail(ae.getMessage());
        }

        // check get response
        Partners responseEntityObject = ServiceHelper.get(getWebTarget_GetAll(), MediaType.APPLICATION_JSON, Partners.class);
        Assert.assertEquals(PARTNERS.getPartners().size(), responseEntityObject.getPartners().size());
        Partner updatedPartner = getPartnerById(partnerForUpdate.getId(), responseEntityObject);
        // validate added partner
        try {
            validatePartnerEquals(partnerForUpdate, updatedPartner);
        } catch (AssertionError ae) {
            Assert.fail(ae.getMessage());
        }
    }

    /**
     * test check for {com.comcast.redirector.api.decider.PartnersController#addOrUpdatePartner(Partner, javax.ws.rs.core.UriInfo)}
     */
    @Test
    public void testUpdateParticularPartner_XmlObject() {
        // post our partners
        ServiceHelper.post(getWebTarget_Post(), PARTNERS, MediaType.APPLICATION_XML);

        // update Partner
        Partner partnerForUpdate = createPartner("p1", createPartnerProperty("prop10", "value10"));
        Partner responsePartner = ServiceHelper.put(getWebTarget_Put(), partnerForUpdate, MediaType.APPLICATION_XML);
        // validate response
        try {
            validatePartnerEquals(partnerForUpdate, responsePartner);
        } catch (AssertionError ae) {
            Assert.fail(ae.getMessage());
        }

        // check get response
        Partners responseEntityObject = ServiceHelper.get(getWebTarget_GetAll(), MediaType.APPLICATION_XML, Partners.class);
        Assert.assertEquals(PARTNERS.getPartners().size(), responseEntityObject.getPartners().size());
        Partner updatedPartner = getPartnerById(partnerForUpdate.getId(), responseEntityObject);
        // validate added partner
        try {
            validatePartnerEquals(partnerForUpdate, updatedPartner);
        } catch (AssertionError ae) {
            Assert.fail(ae.getMessage());
        }
    }

    /**
     * test check for {com.comcast.redirector.api.decider.PartnersController#addOrUpdatePartner(Partner, javax.ws.rs.core.UriInfo)}
     */
    @Test
    public void testUpdateParticularPartner_JsonString() {
        // updated partner "p1"
        final String JSON_UpdatedPartner = "{\"id\":\"p1\",\"properties\":{\"property\":[{\"name\":\"prop10\",\"value\":\"val10\"}]}}";
        Partner partnerForUpdate = createPartner("p1", createPartnerProperty("prop10", "value10"));

        // post our partners
        ServiceHelper.post(getWebTarget_Post(), PARTNERS, MediaType.APPLICATION_JSON);

        // update Partner "p1"
        String responsePartner = ServiceHelper.put(getWebTarget_Put(), JSON_UpdatedPartner, MediaType.APPLICATION_JSON);
        // validate response
        Assert.assertEquals(JSON_UpdatedPartner, responsePartner);

        // check get response
        Partners responseEntityObject = ServiceHelper.get(getWebTarget_GetAll(), MediaType.APPLICATION_JSON, Partners.class);
        Assert.assertEquals(PARTNERS.getPartners().size(), responseEntityObject.getPartners().size());
        Partner updatedPartner = getPartnerById(partnerForUpdate.getId(), responseEntityObject);
        // validate added partner
        try {
            validatePartnerEquals(partnerForUpdate, updatedPartner);
        } catch (AssertionError ae) {
            Assert.fail(ae.getMessage());
        }
    }

    /**
     * test check for {com.comcast.redirector.api.decider.PartnersController#addOrUpdatePartner(Partner, javax.ws.rs.core.UriInfo)}
     */
    @Test
    public void testUpdateParticularPartner_XmlString() {
        // updated partner "p1"
        final String XML_UpdatedPartner = "<partner id=\"p1\"><properties><property name=\"prop10\">value10</property></properties></partner>";
        Partner partnerForUpdate = createPartner("p1", createPartnerProperty("prop10", "value10"));

        // post our partners
        ServiceHelper.post(getWebTarget_Post(), PARTNERS, MediaType.APPLICATION_XML);

        // update Partner "p1"
        String responsePartner = ServiceHelper.put(getWebTarget_Put(), XML_UpdatedPartner, MediaType.APPLICATION_XML);
        // validate response
        Assert.assertThat(responsePartner, containsString(XML_UpdatedPartner));

        // check get response
        Partners responseEntityObject = ServiceHelper.get(getWebTarget_GetAll(), MediaType.APPLICATION_XML, Partners.class);
        Assert.assertEquals(PARTNERS.getPartners().size(), responseEntityObject.getPartners().size());
        Partner updatedPartner = getPartnerById(partnerForUpdate.getId(), responseEntityObject);
        // validate added partner
        try {
            validatePartnerEquals(partnerForUpdate, updatedPartner);
        } catch (AssertionError ae) {
            Assert.fail(ae.getMessage());
        }
    }

    /**
     * test check for {com.comcast.redirector.api.decider.PartnersController#getPartnerById(String)}
     */
    @Test
    public void testGetParticularPartner_JsonObject() {
        // post our partners
        ServiceHelper.post(getWebTarget_Post(), PARTNERS, MediaType.APPLICATION_JSON);

        // get partner
        Partner partner = PARTNERS.getPartners().iterator().next();
        Partner responseEntityObject = ServiceHelper.get(getWebTarget_GetOne(partner.getId()),
                MediaType.APPLICATION_JSON, Partner.class);
        // validate response
        try {
            validatePartnerEquals(partner, responseEntityObject);
        } catch (AssertionError ae) {
            Assert.fail(ae.getMessage());
        }
    }

    /**
     * test check for {com.comcast.redirector.api.decider.PartnersController#getPartnerById(String)}
     */
    @Test
    public void testGetParticularPartner_XmlObject() {
        // post our partners
        ServiceHelper.post(getWebTarget_Post(), PARTNERS, MediaType.APPLICATION_XML);

        // get partner
        Partner partner = PARTNERS.getPartners().iterator().next();
        Partner responseEntityObject = ServiceHelper.get(getWebTarget_GetOne(partner.getId()),
                MediaType.APPLICATION_XML, Partner.class);
        // validate response
        try {
            validatePartnerEquals(partner, responseEntityObject);
        } catch (AssertionError ae) {
            Assert.fail(ae.getMessage());
        }
    }

    /**
     * test check for {com.comcast.redirector.api.decider.PartnersController#getPartnerById(String)}
     */
    @Test
    public void testGetParticularPartner_JsonString() {
        // post our partners
        ServiceHelper.post(getWebTarget_Post(), PARTNERS, MediaType.APPLICATION_JSON);

        // get partner
        String partnerId = "p2";
        String responseEntityObject = ServiceHelper.get(getWebTarget_GetOne(partnerId), MediaType.APPLICATION_JSON, String.class);
        // validate response
        Assert.assertEquals(JSON_PARTNER_2, responseEntityObject);
    }

    /**
     * test check for {com.comcast.redirector.api.decider.PartnersController#getPartnerById(String)}
     */
    @Test
    public void testGetParticularPartner_XmlString() {
        // post our partners
        ServiceHelper.post(getWebTarget_Post(), PARTNERS, MediaType.APPLICATION_XML);

        // get partner
        String partnerId = "p2";
        String responseEntityObject = ServiceHelper.get(getWebTarget_GetOne(partnerId), MediaType.APPLICATION_XML, String.class);
        // validate response
        Assert.assertThat(responseEntityObject, containsString(XML_PARTNER_2));
    }

    /**
     * test check for {com.comcast.redirector.api.decider.PartnersController#deletePartner(String, javax.ws.rs.core.UriInfo)}
     */
    @Test
    public void testDeletePartner() {
        // post our partners
        ServiceHelper.post(getWebTarget_Post(), PARTNERS, MediaType.APPLICATION_JSON);

        // delete first partner
        Partner partnerForDelete = PARTNERS.getPartners().iterator().next();
        ServiceHelper.delete(getWebTarget_Delete(partnerForDelete.getId()));

        // check get response
        Partners responseEntityObject = ServiceHelper.get(getWebTarget_GetAll(), MediaType.APPLICATION_JSON, Partners.class);
        // validate response
        Assert.assertEquals(PARTNERS.getPartners().size() - 1, responseEntityObject.getPartners().size());
        Assert.assertNull(getPartnerById(partnerForDelete.getId(), responseEntityObject));
    }

    private void validatePartnersEquals(Partners expectedPartners, Partners actualPartners) throws AssertionError {
        Assert.assertEquals("Partners size assertion error:",
                expectedPartners.getPartners().size(), actualPartners.getPartners().size());
        for (Partner expectedPartner : expectedPartners.getPartners()) {
            Partner actualPartner = getPartnerById(expectedPartner.getId(), actualPartners);
            validatePartnerEquals(expectedPartner, actualPartner);
        }
    }

    private void validatePartnerEquals(Partner expectedPartner, Partner actualPartner) throws AssertionError {
        Assert.assertNotNull("Not found assertion error, Partner not found with Id: "
                + expectedPartner.getId() + "\n", actualPartner);
        validatePartnerPropertiesEquals(expectedPartner.getProperties(), actualPartner.getProperties());
    }

    private void validatePartnerPropertiesEquals(Set<PartnerProperty> expectedProperties,
                                                 Set<PartnerProperty> actualProperties) throws AssertionError {
        PartnerProperty[] expectedItems = expectedProperties.toArray(new PartnerProperty[expectedProperties.size()]);
        Assert.assertThat("Partner properties assertion error:", actualProperties, hasItems(expectedItems));
    }

    private Partner getPartnerById(String partnerId, Partners partners) {
        for (Partner partner: partners.getPartners()) {
            if (partnerId.equals(partner.getId())) {
                return partner;
            }
        }
        return null;
    }

    private static Partners createDefaultPartners() {
        return createPartners(
                createPartner("p1",
                        createPartnerProperty("prop1", "val1"),
                        createPartnerProperty("prop2", "val2")
                ),
                createPartner("p2",
                        createPartnerProperty("prop3", "val3")
                )
        );
    }

    private static Partners createPartners(Partner... partners) {
        Partners result = new Partners();
        for (Partner partner: partners) {
            result.addPartner(partner);
        }
        return result;
    }

    private static Partner createPartner(String partnerId, final PartnerProperty... partnerProperties) {
        Partner partner = new Partner(partnerId);
        partner.setProperties(new LinkedHashSet<>(Arrays.asList(partnerProperties)));
        return partner;
    }

    private Partners createEmptyPartners() {
        Partners partners = new Partners();
        partners.setPartners(new LinkedHashSet<Partner>() {{
            add(new Partner());
        }});
        return partners;
    }

    private static PartnerProperty createPartnerProperty(String propertyName, String propertyValue) {
        PartnerProperty partnerProperty = new PartnerProperty();
        partnerProperty.setName(propertyName);
        partnerProperty.setValue(propertyValue);
        return  partnerProperty;
    }

    private WebTarget getWebTarget_Post() {
        return HttpTestServerHelper.target().path(DECIDER_PARTNERS_PATH);
    }

    private WebTarget getWebTarget_GetAll() {
        return HttpTestServerHelper.target().path(DECIDER_PARTNERS_PATH);
    }

    private WebTarget getWebTarget_GetOne(String partnerId) {
        return HttpTestServerHelper.target().path(DECIDER_PARTNERS_PATH).path(partnerId);
    }

    private WebTarget getWebTarget_Put() {
        return HttpTestServerHelper.target().path(DECIDER_PARTNERS_PATH);
    }

    private WebTarget getWebTarget_Delete(String partnerId) {
        return HttpTestServerHelper.target().path(DECIDER_PARTNERS_PATH).path(partnerId);
    }

}

