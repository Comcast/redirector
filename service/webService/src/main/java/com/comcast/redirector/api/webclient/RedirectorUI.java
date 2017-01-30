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

package com.comcast.redirector.api.webclient;

import org.glassfish.jersey.server.mvc.Viewable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

@Path("/")
public class RedirectorUI {

    private static Logger log = LoggerFactory.getLogger(RedirectorUI.class);

    @Context
    private UriInfo uriInfo;

    @Context
    HttpServletRequest request;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response defaultPage(@Context UriInfo ui) throws URISyntaxException {
        /*
        * This redirect is required due to change of "Jersey" version from "1.17" to "2.13".
        * The "1.*" version of jersey has property "FEATURE_REDIRECT".
        * For example, when making request "localhost:8888/context/dev", Jersey checks whether "FEATURE_REDIRECT" is set to "true" in ServletContainer and request does not end with '/'.
        * If so, trailing slash is added and redirect is occurred to "localhost:8888/context/dev/"
        *
        * Jersey "2.*" does not contain property "FEATURE_REDIRECT".
        * The code that made redirect in "1.*" jersey is commented out in ServletContainer.java:504
        * Jersey "2.*" resolves request even if '/' was not present in the end.
        * But all links in our *.jsp and *.html to *.js and *.css are relative. So without adding '/' in the end, files can not be opened.
        * To solve it, we introduced this redirect
        */
        if (!ui.getAbsolutePath().toString().endsWith("/")) {
            return Response.temporaryRedirect(new URI(ui.getAbsolutePath().toString() + "/")).build();
        } else {
            return Response.ok(new Viewable("/index.jsp", new HashMap<String, Object>())).build();
        }
    }

    @GET
    @Path("/index.html")
    @Produces(MediaType.TEXT_HTML)
    public Response indexPage(@Context UriInfo ui) throws URISyntaxException {
        return defaultPage(ui);
    }
}
