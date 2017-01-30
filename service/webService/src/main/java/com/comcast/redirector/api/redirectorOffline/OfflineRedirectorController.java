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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */


package com.comcast.redirector.api.redirectorOffline;

import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.api.model.namespaced.NamespaceChangesStatus;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.OfflineChangesStatus;
import com.comcast.redirector.api.model.SnapshotList;
import com.comcast.redirector.api.redirector.service.NamespacesChangesService;
import com.comcast.redirector.api.redirectorOffline.service.RedirectorOfflineModeService;
import com.comcast.redirector.common.serializers.JSONSerializer;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.SerializerException;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

@Component
@Path(RedirectorConstants.REDIRECTOR_OFFLINE_CONTROLLER_PATH)
public class OfflineRedirectorController {
    private static final String NAMESPACES_CHANGES_SERVICE_NAME = "namespacesChanges";

    @Autowired
    private RedirectorOfflineModeService offlineModeService;

    @Autowired
    private NamespacesChangesService namespacesChangesService;

    @Autowired
    private Serializer jsonSerializer;

    @GET
    @Path("{serviceName}/modelSnapshot")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getSanpshot(@PathParam("serviceName") String serviceName) {
        return Response.ok(offlineModeService.getSnapshot(serviceName)).build();
    }

    @GET
    @Path("namespaceSnapshot")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getNamespacesSnapshot() {
        return Response.ok(offlineModeService.getNamespacedList()).build();
    }

    @GET
    @Path("applications")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getApplicationsSnapshot() {
        return Response.ok(offlineModeService.getApplicationNames()).build();
    }

    @GET
    @Path("allSnapshots")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllSnapshot() {
        return Response.ok(offlineModeService.getAllSnapshots()).build();
    }

    @POST
    @Path("downloadCoreBackup")
    @Consumes({MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_OCTET_STREAM})
    public Response downloadCoreBackup(@FormParam("snapshots") String jsonSnapshots){
        try {
            SnapshotList snapshotList = jsonSerializer.deserialize(jsonSnapshots, SnapshotList.class);
            return Response.ok(offlineModeService.createXREApplicationBackup(snapshotList)).header("content-disposition", "attachment; filename = coreBackup.zip").build();
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Path("{serviceName}/getOfflinePendingChanges")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({MediaType.APPLICATION_JSON})
    public Response uploadCoreBackup(
            @PathParam("serviceName") String serviceName,
            @FormDataParam("file") InputStream zipByteArray,
            @FormDataParam("file") FormDataContentDisposition fileDisposition) throws IOException, SerializerException {
        return Response.ok(offlineModeService.calculateOfflinePendingChanges(serviceName, zipByteArray)).build();
    }

    @POST
    @Path("{serviceName}/hasOfflinePendingChanges")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.TEXT_PLAIN})
    public Response hasPendingChanges(
            @PathParam("serviceName") String serviceName) {
        return Response.ok(offlineModeService.hasOfflinePendingChanges(serviceName)).build();
    }

    @POST
    @Path("getAllNamespacesChanges")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getAllNamespacesChanges(final String servicename) throws IOException, SerializerException {
        return Response.ok(namespacesChangesService.getNamespaceChangesStatus(NAMESPACES_CHANGES_SERVICE_NAME)).build();

    }

    @POST
    @Path("cancelAllNamespacesChanges")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response cancelAllNamespacesChanges(final String servicename) throws IOException, SerializerException{
        return Response.ok(namespacesChangesService.cancelAll(NAMESPACES_CHANGES_SERVICE_NAME)).build();

    }

    @POST
    @Path("cancelNamespacesChanges")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response cancelNamespacesChanges(final NamespacedList namespacedList) throws IOException, SerializerException {
        return Response.ok(namespacesChangesService.cancel(NAMESPACES_CHANGES_SERVICE_NAME, namespacedList)).build();

    }

    @POST
    @Path("approveNamespacesChanges")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response approveNamespacesChanges(final NamespacedList namespacedList) throws IOException, SerializerException {
        return Response.ok(namespacesChangesService.approve(NAMESPACES_CHANGES_SERVICE_NAME, namespacedList)).build();

    }

    @POST
    @Path("approveAllNamespacesChanges")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response approveAllNamespacesChanges(
            final NamespaceChangesStatus namespaceChangesStatus) throws IOException, SerializerException {
        return Response.ok(namespacesChangesService.approveAll(NAMESPACES_CHANGES_SERVICE_NAME)).build();
    }

    @POST
    @Path("{serviceName}/hasOfflineChanges")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.TEXT_PLAIN})
    public Response hasChanges(final OfflineChangesStatus offlineChangesStatus) {
        return Response.ok(offlineModeService.hasOfflineChanges(offlineChangesStatus)).build();
    }


}
