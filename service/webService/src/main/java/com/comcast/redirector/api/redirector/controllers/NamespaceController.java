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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */

package com.comcast.redirector.api.redirector.controllers;

import com.comcast.redirector.api.model.ErrorMessage;
import com.comcast.redirector.api.model.ExpressionValidationException;
import com.comcast.redirector.api.model.SnapshotList;
import com.comcast.redirector.api.model.namespaced.*;
import com.comcast.redirector.api.model.search.NamespacedListEntity;
import com.comcast.redirector.api.model.search.NamespacedListSearchResult;
import com.comcast.redirector.api.model.validation.ValidationState;
import com.comcast.redirector.api.model.validation.visitor.redirector.ModelValidationFacade;
import com.comcast.redirector.api.redirector.service.IDataChangesNotificationService;
import com.comcast.redirector.api.redirector.service.INamespacedListsService;
import com.comcast.redirector.api.redirector.service.export.IExportFileNameHelper;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.dao.NamespacedListsDAO;
import jersey.repackaged.com.google.common.base.Splitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.*;

@Component
@Path(RedirectorConstants.NAMESPACE_CONTROLLER_PATH)
public class NamespaceController {
    @Autowired
    private INamespacedListsService namespacedListsService;

    @Autowired
    private IExportFileNameHelper exportFileNameHelper;

    @Autowired
    private IDataChangesNotificationService dataChangesNotificationService;

    @POST
    @Path("validate")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response validateNamespacedList(final SnapshotList snapshotList) {
        NamespacedList namespace = (NamespacedList)snapshotList.getEntityToSave();

        if (!namespacedListsService.getNamespaceDuplicates(namespace, snapshotList.getNamespaces()).isEmpty()) {
            ValidationState validationState = new ValidationState();
            validationState.pushError(ValidationState.ErrorType.NamespacedListsDuplicates);
            throw new WebApplicationException(new ExpressionValidationException("", validationState));
        }

        try {
            ModelValidationFacade.validateNamespacedList(namespace);
        } catch (ExpressionValidationException ex) {
            String error = String.format("Failed to save namespace '%s' due to validation error(s). %s",  namespace.getName(), ex.getMessage());
            throw new WebApplicationException(error, ex, Response.Status.BAD_REQUEST);
        }

        return Response.ok(namespace).build();
    }

    @GET
    @Path("/")
    @Deprecated
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllNamespacesOldFormat() {
        Namespaces namespaces = namespacedListsService.getAllNamespacedListsFilteredByPermissions();
        for (NamespacedList namespacedList: namespaces.getNamespaces()) {
            //old format conversion
            NamespacedListsDAO.convertToBackendFormat(namespacedList);
        }
        return Response.ok(namespaces).build();
    }

    @GET
    @Path("/getAllNamespacedLists")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllNamespaces() {
        Namespaces namespaces = namespacedListsService.getAllNamespacedListsFilteredByPermissions();
        return Response.ok(namespaces).build();
    }

    @GET
    @Path("/getAllNamespacedListsWithoutValues")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllNamespacesWithoutValues() {
        Namespaces namespaces = namespacedListsService.getAllNamespacedListsWithoutValues();
        return Response.ok(namespaces).build();
    }

    @GET
    @Path("getOne/{name}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getNamespace(@PathParam("name") final String name) {
        NamespacedList namespacedList = namespacedListsService.getNamespacedListByName(name);
        if (namespacedList == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        else {
            return Response.ok(namespacedList).build();
        }
    }

    @GET
    @Deprecated
    @Path("{name}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getNamespaceOldFormat(@PathParam("name") final String name) {
        NamespacedList namespacedList = namespacedListsService.getNamespacedListByName(name);
        if (namespacedList == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        else {
            NamespacedListsDAO.convertToBackendFormat(namespacedList);
            return Response.ok(namespacedList).build();
        }
    }


    @GET
    @Path("search/{value}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response searchNamespacedLists(@PathParam("value") final String value) {
        NamespacedListSearchResult result = namespacedListsService.searchNamespacedLists(value);
        return Response.ok(result).build();
    }

    @GET
    @Path("export")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getExportNamespaces() {
        return Response.ok(namespacedListsService.getAllNamespacedListsFilteredByPermissions())
                .header(exportFileNameHelper.getHeader(), exportFileNameHelper.getFileNameForAll(EntityType.NAMESPACED_LIST))
                .build();
    }

    @GET
    @Path("export/{name}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getExportNamespace(@PathParam("name") final String name) {
        List<NamespacedList> result = new ArrayList<>();
        Namespaces namespaces = new Namespaces();
        result.add(namespacedListsService.getNamespacedListByName(name));
        namespaces.setNamespaces(result);
        return Response.ok(namespaces)
                .header(exportFileNameHelper.getHeader(), exportFileNameHelper.getFileNameForOneEntityWithoutService(EntityType.NAMESPACED_LIST, name))
                .build();
    }

    @POST
    @Path("duplicates")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response searchNamespaceDuplicates(final NamespacedList newNamespacedList) {
        Namespaces allNamespaces = namespacedListsService.getAllNamespacedLists();
        return Response.ok(namespacedListsService.getNamespaceDuplicatesFilteredByPermissions(newNamespacedList, allNamespaces)).build();
    }

    @POST
    @Deprecated
    @Path("{name}/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response validateNamespacedListOldFormat(@PathParam("name") final String name, final NamespacedList namespacedList, @Context UriInfo ui) {
        NamespacedListsDAO.convertToFrontendFormat(namespacedList);
        NamespacedList addedNamespacedList = namespacedListsService.addNamespacedList(name, namespacedList, false);
        NamespacedListsDAO.convertToBackendFormat(addedNamespacedList);
        return Response.created(ui.getRequestUri()).entity(addedNamespacedList).build();
    }

    @POST
    @Path("addNewNamespaced/{name}/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response validateNamespacedList(@PathParam("name") final String name,  @QueryParam(value = "autoResolve") @DefaultValue("false") boolean autoResolve, final NamespacedList namespacedList,
                                           @Context UriInfo ui) {
        NamespacedList addedNamespacedList = namespacedListsService.addNamespacedList(name, namespacedList, autoResolve);
        return Response.created(ui.getRequestUri()).entity(addedNamespacedList).build();
    }

    @DELETE
    @Path("{name}/")
    public void deleteNamespacedList(@PathParam("name") final String name) {
        namespacedListsService.deleteNamespacedList(name);
    }

    //todo: it is not used anywhere in the UI and is unnecessary.
    @DELETE
    @Path("{name}/{values: (\\S+)}/")
    @Deprecated
    public void deleteNamespacedListValues(@PathParam("name") final String name, @PathParam("values") final String values) {
        NamespacedList list = namespacedListsService.getNamespacedListByName(name);
        if (list == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage("Namespaced list is not found with name: " + name)).build());
        }
        List val = Arrays.asList(values.split(","));
        for (Object item : val) {
            list.getValueSet().remove(new NamespacedListValueForWS((String) item));
        }
        namespacedListsService.addNamespacedList(list);
    }

    // TODO: do we need this API? It is not used in the UI, but there is a ticket for it (APPDS-1151)
    @PUT
    @Path("{name}/addValues")
    public Response addNamespacedListValues(@PathParam("name") final String name, final NamespacedList namespacedListValues) {
        NamespacedList namespacedList = namespacedListsService.getNamespacedListByName(name);
        if (namespacedList == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage("Namespaced list is not found with name: " + name)).build());
        }

        Namespaces allNamespaces = namespacedListsService.getAllNamespacedLists();
        if (namespacedListsService.getNamespaceDuplicates(namespacedListValues, allNamespaces).isEmpty()) {
            namespacedList.getValueSet().addAll(namespacedListValues.getValueSet());
            namespacedListsService.addNamespacedList(namespacedList);
            return Response.ok(namespacedList).build();
        } else {
            ValidationState validationState = new ValidationState();
            validationState.pushError(ValidationState.ErrorType.NamespacedListsDuplicates);
            throw new WebApplicationException(new ExpressionValidationException("", validationState));
        }
    }

    @GET
    @Path("dependingRulesMultiple/{namespacedNames}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getRulesDependentOnMultipleNamespaced (@PathParam("namespacedNames") String namespacedNames) {
        Iterable<String> namespacedIterable = Splitter.on(',').split(namespacedNames);
        NamespacedListSearchResult result = new NamespacedListSearchResult();
        List<NamespacedListEntity> namespacedListEntities = new LinkedList<>();
        for (String namespacedName : namespacedIterable) {
            NamespacedListEntity namespacedList =  namespacedListsService.getRulesDependingOnNamespaced(namespacedName);
            namespacedList.setName(namespacedName);
            namespacedListEntities.add(namespacedList);
        }

        result.setNamespacedLists(namespacedListEntities);
        return Response.ok(result).build();
    }

    //toDo: this is a case of {@link deleteEntitiesFromMultipleNamespacedLists}, need to decide what to do
    @POST
    @Path("deleteNamespacedEntities/{name}")
    public Response deleteNamespacedListValuesBatch(@PathParam("name") final String name, NamespacedEntities values) {
        try {
            NamespacedEntities returnedEntities;
            NamespacedList list = namespacedListsService.getNamespacedListByName(name);
            returnedEntities = namespacedListsService.removeEntitiesFromNSListAndReturnNotFoundAndDeletedValues(list, values);
            namespacedListsService.addNamespacedList(list);
            return Response.ok(returnedEntities).build();
        } catch (WebApplicationException ex) {
            return Response.status(ex.getResponse().getStatus()).build();
        }
    }

    @POST
    @Path("deleteEntitiesFromNamespacedLists/")
    public Response deleteEntitiesFromMultipleNamespacedLists(List<NamespacedValuesToDeleteByName> toDelete) {
        Response response =  Response.ok(namespacedListsService.deleteEntitiesFromMultipleNamespacedLists(toDelete)).build();
        return response;
    }

    @GET
    @Path("getVersion/")
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    public Response getVersion() {
        long version = dataChangesNotificationService.getNamespacedListsVersion();
        return Response.ok(version).build();
    }
}
