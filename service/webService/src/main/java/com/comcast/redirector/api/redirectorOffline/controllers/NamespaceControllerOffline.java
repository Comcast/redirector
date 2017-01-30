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


package com.comcast.redirector.api.redirectorOffline.controllers;

import com.comcast.redirector.api.ApplicationStatusMode;
import com.comcast.redirector.api.model.*;
import com.comcast.redirector.api.model.namespaced.*;
import com.comcast.redirector.api.model.offlinemode.OperationResult;
import com.comcast.redirector.api.model.search.NamespacedListEntity;
import com.comcast.redirector.api.model.search.NamespacedListSearchResult;
import com.comcast.redirector.api.model.validation.visitor.redirector.ModelValidationFacade;
import com.comcast.redirector.api.redirector.service.INamespacedListsService;
import com.comcast.redirector.common.RedirectorConstants;
import jersey.repackaged.com.google.common.base.Splitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Component
@Path(RedirectorConstants.NAMESPACE_OFFLINE_CONTROLLER_PATH)
public class NamespaceControllerOffline {

    @Autowired
    private INamespacedListsService namespacedListsService;

    @POST
    @Path("validate")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addNamespacedList(final SnapshotList snapshotList) {
        NamespacedList namespace = (NamespacedList)snapshotList.getEntityToSave();

        if (!namespacedListsService.getNamespaceDuplicates(namespace, snapshotList.getNamespaces()).isEmpty()) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage("There are duplicates in the namespaced list.")).build());
        }

        try {
            ModelValidationFacade.validateNamespacedList(namespace);
            namespace.updateVersion();
            namespace.setValueCount(namespace.getValueSet().size());
        } catch (ExpressionValidationException ex) {
            String error = String.format("Failed to save namespace '%s' due to validation error(s). %s",  namespace.getName(), ex.getMessage());
            throw new WebApplicationException(error, ex, Response.Status.BAD_REQUEST);
        }

        return Response.ok(namespace).build();
    }

    @POST
    @Path("search/{value}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response searchNamespacedLists(@PathParam("value") final String value, final SnapshotList snapshotList) {
        NamespacedListSearchResult result = namespacedListsService.searchNamespacedLists(new NamespacedListValueForWS(value), snapshotList);
        return Response.ok(result).build();
    }

    @POST
    @Path("duplicates")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response searchDuplicates(final Namespaces namespaces) {
        return Response.ok(namespacedListsService.getNamespaceDuplicates(namespaces.getNewNamespace(), namespaces)).build();
    }

    @POST
    @Path("{name}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deleteNamespace(@PathParam("name") final String name, SnapshotList snapshotList) {

        NamespacedList nsListToDelete = (NamespacedList)snapshotList.getEntityToSave();
        for (NamespacedList namespacedList: snapshotList.getNamespaces().getNamespaces()) {
            if (namespacedList.getName().equals(name)) {
                nsListToDelete = namespacedList;
            }
        }

        Collection<IfExpression> rules = new ArrayList<>();
        for (Snapshot snapshot : snapshotList.getItems()) {
            rules.addAll(snapshot.getFlavorRules().getItems());
            rules.addAll(snapshot.getUrlRules().getItems());
            rules.addAll(snapshot.getTemplatePathRules().getItems());
            rules.addAll(snapshot.getTemplateUrlRules().getItems());
        }

        namespacedListsService.validateNamespacedList(nsListToDelete, rules);

        return Response.ok().build();
    }

    @DELETE
    @Path("{name}/{values: (\\S+)}/")
    public Response deleteNamespacedListValues(@PathParam("name") final String name, @PathParam("values") final String values, final NamespacedList list) {
        if (list == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage("Namespaced list is null: ")).build());
        }
        List val = Arrays.asList(values.split(","));
        for (Object item : val) {
            list.getValueSet().remove(new NamespacedListValueForWS((String) item));
        }
        list.updateVersion();
        return Response.ok(list).build();
    }

    @POST
    @Path("dependingRulesMultiple/{namespacedNames}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getRulesDependentOnMultipleNamespaced (@PathParam("namespacedNames") String namespacedNames, SnapshotList snapshotList) {
        Iterable<String> namespacedIterable = Splitter.on(',').split(namespacedNames);
        NamespacedListSearchResult result = new NamespacedListSearchResult();
        List<NamespacedListEntity> namespacedListEntities = new LinkedList<>();
        for (String namespacedName : namespacedIterable) {
            NamespacedListEntity namespacedList =  namespacedListsService.getRulesDependingOnNamespaced(namespacedName, snapshotList);
            namespacedList.setName(namespacedName);
            namespacedListEntities.add(namespacedList);
        }

        result.setNamespacedLists(namespacedListEntities);
        return Response.ok(result).build();
    }

    @POST
    @Path("deleteNamespacedEntities/{name}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response deleteNamespacedListValuesBatch(@PathParam("name") final String name, SnapshotList snapshotList) {
        try {
            NamespacedEntities returnedEntities;
            NamespacedEntities values = (NamespacedEntities)snapshotList.getEntityToSave();
            NamespacedList list = snapshotList.getNamespaces().getNamespaceByName(name);
            returnedEntities = namespacedListsService.removeEntitiesFromNSListAndReturnNotFoundAndDeletedValues(list, values);
            OperationResult result = new OperationResult();
            list.updateVersion();
            result.setApprovedEntity(list);
            result.setMethodResponce(returnedEntities);
            return Response.ok(result).build();
        } catch (WebApplicationException ex) {
            return Response.status(ex.getResponse().getStatus()).build();
        }

    }

    @POST
    @Path("deleteEntitiesFromNamespacedLists/")
    public Response deleteEntitiesFromMultipleNamespacedLists(List<NamespacedValuesToDeleteByName> toDelete) {
        return Response.ok(namespacedListsService.deleteEntitiesFromMultipleNamespacedLists(toDelete, ApplicationStatusMode.OFFLINE)).build();
    }
}
