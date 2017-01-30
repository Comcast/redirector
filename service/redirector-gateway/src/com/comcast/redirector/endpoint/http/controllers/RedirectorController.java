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

package com.comcast.redirector.endpoint.http.controllers;

import com.comcast.redirector.core.config.ConfigLoader;
import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.endpoint.http.Constants;
import com.comcast.redirector.endpoint.http.model.Location;
import com.comcast.redirector.endpoint.http.model.RedirectLocations;
import com.comcast.redirector.endpoint.http.services.RedirectorService;
import com.comcast.xre.common.redirector.selector.xmlimpl.Config;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping(Constants.SERVICE_URL_PREFIX)
public class RedirectorController {

    @Autowired
    private RedirectorService redirectorService;

    private static ZKConfig zkConfig = ConfigLoader.doParse(Config.class);

    @RequestMapping(value="/{appName}",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public RedirectLocations redirectAppPOST(@PathVariable String appName,
                                  @RequestBody Map<String, String> params,
                                  @RequestHeader HttpHeaders headers,
                                  HttpServletResponse response) throws IOException {
        return redirectInternal(appName, params, headers, response);
    }

    @RequestMapping(value="/{appName}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public RedirectLocations redirectAppGET(@PathVariable String appName, @RequestParam Map<String, String> params, @RequestHeader HttpHeaders headers,
                                  HttpServletResponse response) throws IOException {
        return redirectInternal(appName, params, headers, response);
    }

    private RedirectLocations redirectInternal(String appName, Map<String, String> params, HttpHeaders headers,
                                               HttpServletResponse response) throws IOException {
        RedirectLocations locations = redirectorService.redirect(appName, params, headers);

        if (zkConfig.getRedirectWith302()) {
            if (locations == null || locations.getLocation() == null || locations.getLocation().isEmpty()) {
                response.setStatus(HttpStatus.SC_NOT_FOUND);
                return null;
            }
            Location locationToRedirect = locations.getLocation().get(0);
            String url = locationToRedirect.getProtocol() +
                    "://" +
                    locationToRedirect.getDestination() +
                    ":" +
                    locationToRedirect.getPort();

            response.sendRedirect(url);
        }
        return locations;
    }
}
