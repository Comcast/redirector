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

package com.comcast.redirector.api.decider;

import com.comcast.redirector.api.decider.service.IPartnersService;
import com.comcast.redirector.api.model.appDecider.Partner;
import com.comcast.redirector.api.model.appDecider.Partners;
import com.comcast.redirector.common.DeciderConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Component
@Path(DeciderConstants.DECIDER_PARTNERS_PATH)
public class PartnersController {
    @Autowired
    private IPartnersService partnersService;

    @GET
    @Path("")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllPartners(@Context UriInfo ui) {
        return Response.ok(partnersService.getAllPartners()).build();
    }

    @GET
    @Path("{partnerId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getPartnerById(@PathParam("partnerId")String partnerId) {
        return Response.ok(partnersService.getPartnerById(partnerId)).build();
    }

    @GET
    @Path("export")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAllPartnersForExport(@Context UriInfo ui) {
        return Response.ok(partnersService.getAllPartners())
            .header("content-disposition", "attachment; filename = exportedAllPartners.json")
            .build();
    }

    @GET
    @Path("export/{partnerId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getPartnersForExportById(@PathParam("partnerId")String partnerId) {
        Partners partners = new Partners();
        partners.addPartner(partnersService.getPartnerById(partnerId));
        return Response.ok(partners)
                .header("content-disposition", "attachment; filename = exportedPartner_" + partnerId + ".json")
                .build();
    }

    @POST
    @Path("")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response savePartners(Partners partners, @Context UriInfo ui) {
        partnersService.savePartners(partners);
        return Response.created(ui.getRequestUri()).entity(partners).build();
    }

    @PUT
    @Path("")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addOrUpdatePartner(Partner partner, @Context UriInfo ui) {
        partnersService.savePartner(partner);
        return Response.created(ui.getRequestUri()).entity(partner).build();
    }

    @DELETE
    @Path("{partnerId}/")
    public void deletePartner(@PathParam("partnerId") final String partnerId) {
        partnersService.deletePartner(partnerId);
    }

}
