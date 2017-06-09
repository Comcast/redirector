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

package com.comcast.redirector.api.redirector.service;

import com.comcast.redirector.api.model.ErrorMessage;
import com.comcast.redirector.api.model.StackComment;
import com.comcast.redirector.api.model.xrestack.PathItem;
import com.comcast.redirector.api.model.xrestack.ServicePaths;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.dataaccess.dao.IListServiceDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;

@Service
public class StackCommentsService implements IStackCommentsService {
    private static Logger log = LoggerFactory.getLogger(StackCommentsService.class);

    @Autowired
    @Qualifier("stackCommentsDAO")
    private IListServiceDAO<StackComment> stackCommentsDAO;

    @Autowired
    private StacksService stacksService;

    @Override
    public StackComment getComment(String path, String serviceName) {
        if (path == null || "".equals(path)) {
            return new StackComment();
        }
        StackComment comment = stackCommentsDAO.getById(serviceName, pathToId(path));
        return comment != null ? comment: new StackComment("");
    }

    @Override
    public List<StackComment> getAllComments(String serviceName) {
        return stackCommentsDAO.getAll(serviceName);
    }

    @Override
    public synchronized void saveComment(StackComment comment, String path, String serviceName) {
        ServicePaths servicePaths = stacksService.getStacksForService(serviceName);
        for (PathItem fullPath: servicePaths.getPaths(serviceName).getStacks()) {
            if(getPathFromFullPath(fullPath.getValue()).equals(path)) {
                try {
                    stackCommentsDAO.saveById(comment, serviceName, pathToId(path));
                } catch (SerializerException e) {
                    throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ErrorMessage(e.getMessage())).build());
                }
                return;
            }
        }
        throw new WebApplicationException(
                Response.status(Response.Status.BAD_REQUEST).entity(
                        new ErrorMessage("Tried to add comment to non-existing stack")).build());
    }

    /**
     * Full Path example: /PO/POC8/1.61
     * Path example: /PO/POC8
     */
    private String getPathFromFullPath (String path) {
        return path.substring(0, path.lastIndexOf("/"));
    }

    private String pathToId (String path) {
        return path.replace("/", "_");
    }
}
