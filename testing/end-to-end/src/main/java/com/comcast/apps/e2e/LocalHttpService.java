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
package com.comcast.apps.e2e;

import com.comcast.apps.e2e.config.E2EConfigLoader;
import com.comcast.apps.e2e.helpers.ServiceHelper;
import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.serializers.JAXBContextBuilder;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.common.serializers.XMLSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LocalHttpService {
    private static final Logger log = LoggerFactory.getLogger(LocalHttpService.class);

    private static final int BACKLOG = 1;
    private static final int NO_RESPONSE_LENGTH = -1;

    private static final String HEADER_ALLOW = "Allow";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final int STATUS_OK = 200;
    private static final int STATUS_METHOD_NOT_ALLOWED = 405;

    private static final String METHOD_GET = "GET";
    private static final String METHOD_OPTIONS = "OPTIONS";
    private static final String ALLOWED_METHODS = METHOD_GET + "," + METHOD_OPTIONS;

    private static final String ROOT_CONTEXT = "/";
    private static final String INDEX_HTML = "/index.html";
    private static final String ALL_APPLICATON_LIST_JSON_NAME = "/list.json";
    private static final String REPORT_JSON = "report.json";

    private static final String APPLICATION_JAVASCRIPT = "application/javascript";
    private static final String APPLICATION_JAVASCRIPT_EXTENSION = ".js";
    private static final String APPLICATION_JSON = "application/json";
    private static final String APPLICATION_JSON_EXTENSION = ".json";
    private static final String TEXT_HTML = "text/html";
    private static final String TEXT_HTML_EXTENSION = ".html";
    private static final String TEXT_CSS = "text/css";
    private static final String TEXT_CSS_EXTENSION = ".css";
    private static final int SLASH_CODE = 47;
    private static final int REVERSE_SLASH_CODE = 92;
    
    private final String webApplicationRootPath = "/webapp";
    private final String reportPath;
    private final String hostname;
    private final int port;

    private ServiceHelper wsServiceHelper = new ServiceHelper(E2EConfigLoader.getDefaultInstance().getServiceBaseUrl());

    private HttpServer server;

    public LocalHttpService(String reportPath, String hostname, String port) throws IOException {
        this.reportPath = reportPath;
        this.hostname = hostname;
        this.port = Integer.valueOf(port);

         createServer();
    }

    private void createServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(hostname, port), BACKLOG);
        server.createContext(ROOT_CONTEXT, getHttpHandler());
    }

    public void start() {
        server.start();
    }

    private HttpHandler getHttpHandler() {
        return new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                try {
                    final Headers headers = httpExchange.getResponseHeaders();
                    final String requestMethod = httpExchange.getRequestMethod().toUpperCase();
                    switch (requestMethod) {
                        case METHOD_GET:
                            final String responseBody = getUrlComponent(httpExchange.getRequestURI());
                            headers.set(HEADER_CONTENT_TYPE,
                                    String.format("%s; charset=%s", TYPE.getMimeType(httpExchange.getRequestURI().toString()), CHARSET));

                            final byte[] rawResponseBody = responseBody.getBytes(CHARSET);
                            httpExchange.sendResponseHeaders(STATUS_OK, rawResponseBody.length);
                            httpExchange.getResponseBody().write(rawResponseBody);
                            break;
                        case METHOD_OPTIONS:
                            headers.set(HEADER_ALLOW, ALLOWED_METHODS);
                            httpExchange.sendResponseHeaders(STATUS_OK, NO_RESPONSE_LENGTH);
                            break;
                        default:
                            headers.set(HEADER_ALLOW, ALLOWED_METHODS);
                            httpExchange.sendResponseHeaders(STATUS_METHOD_NOT_ALLOWED, NO_RESPONSE_LENGTH);
                            break;
                    }
                } finally {
                    httpExchange.close();
                }
            }
        };
    }

    private String getUrlComponent(final URI requestUri) throws JsonProcessingException {
        String request = requestUri.toString();

        if (request.equals(ROOT_CONTEXT)) {
            request = INDEX_HTML;
        }

        if (request.equals(ALL_APPLICATON_LIST_JSON_NAME)) {
            return getExistingApplications(reportPath, REPORT_JSON);
        }

        if (request.endsWith(APPLICATION_JSON_EXTENSION)) {
            try {
                return new String(Files.readAllBytes(Paths.get(reportPath + request)));
            } catch (IOException e) {
                log.warn("Could not find report file in: [" + reportPath + request + "]");
            }
        }

        if (request.contains(RedirectorConstants.RULES_CONTROLLER_PATH)) {
            return getAsXmlFromWebService(request, IfExpression.class);
        }

        if (request.contains(RedirectorConstants.DISTRIBUTION_CONTROLLER_PATH)) {
            return getAsXmlFromWebService(request, Distribution.class);
        }

        String httpPage = null;

        try {
            httpPage = loadComponentFromResourcesPath(webApplicationRootPath + request);
        } catch (IOException e) {
            try {
                return new String(Files.readAllBytes(Paths.get(webApplicationRootPath + request)));
            } catch (IOException ex) {
                log.info("Could not find web component : [" + webApplicationRootPath + request + "]");
            }
        }

        return httpPage;
    }

    private <T extends Expressions> String getAsXmlFromWebService(String request, Class<T> clazz) {
        String result = "";
        T entity = wsServiceHelper.get(request, clazz, MediaType.APPLICATION_JSON);
        if (entity != null) {
            try {
                result = new XMLSerializer(new JAXBContextBuilder().createContextForXML()).serialize(entity);
            } catch (SerializerException e) {
                log.error("Unable to parse " + clazz.getSimpleName() + e);
            }
        }

        return result;
    }

    private String getExistingApplications(final String path, final String reportFileName) throws JsonProcessingException {
        List<String> directories = null;

        try {
            directories = Files.list(Paths.get(path))
                    .filter(Files::isDirectory)
                    .filter(aPath -> Files.exists(Paths.get(aPath + File.separator + reportFileName)))
                    .map(Path::toString)
                    .map(removeRootPath(path))
                    .collect(Collectors.toList());

        } catch (IOException e) {
            log.warn("Could not find report files in: [" + reportPath + "]");
        }

        ObjectMapper mapper = new ObjectMapper();

        return mapper.writeValueAsString(directories);
    }

    private Function<String, String> removeRootPath(final String absolutePath) {
        return new Function<String, String>() {
            @Override
            public String apply(String fileName) {
                String tmp = StringUtils.remove(fileName, removeLastSeparator(absolutePath));
                return removeLeaderSeparator(tmp);
            }
        };
    }

    private String removeLeaderSeparator(final String path) {
        return removeSeparator(path, 0);
    }

    private String removeLastSeparator(final String path) {
        return removeSeparator(path, path.length() - 1);
    }

    private String removeSeparator(final String path, int index) {
        if (path != null && path.length() >= 1 &&
                (path.charAt(index) == SLASH_CODE || path.charAt(index) == REVERSE_SLASH_CODE)) {
            return index == 0 ? path.substring(1) : path.substring(0, index);
        }
        return path;
    }

    private enum TYPE {
        JS(APPLICATION_JAVASCRIPT_EXTENSION, APPLICATION_JAVASCRIPT),
        CSS(TEXT_CSS_EXTENSION, TEXT_CSS),
        JSON(APPLICATION_JSON_EXTENSION, APPLICATION_JSON),
        HTML(TEXT_HTML_EXTENSION, TEXT_HTML);

        private String extension;
        private String mimeType;

        TYPE(final String extension, final String mimeType) {
            this.mimeType = mimeType;
            this.extension = extension;
        }

        public static String getMimeType(final String extension) {
            for (TYPE type : TYPE.values()) {
                if (extension.endsWith(type.extension)) {
                    return type.mimeType;
                }
            }

            return HTML.mimeType;
        }
    }

    private String loadComponentFromResourcesPath(final String filename) throws IOException {
        InputStream inputStream = getClass().getResourceAsStream(filename);

        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader
                (inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        }
        return textBuilder.toString();
    }
}
