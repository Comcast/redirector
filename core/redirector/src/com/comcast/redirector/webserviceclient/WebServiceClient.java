package com.comcast.redirector.webserviceclient;

import com.comcast.redirector.common.logging.ExecutionStep;
import com.comcast.redirector.common.logging.OperationResult;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.serializers.JSONSerializer;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.util.IAuthHeaderProducer;
import com.comcast.redirector.common.util.ThreadLocalLogger;
import com.comcast.redirector.common.util.UrlUtils;
import com.comcast.redirector.dataaccess.client.RestDataSourceExeption;
import com.comcast.redirector.metrics.Metrics;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Map;

import static com.comcast.redirector.common.RedirectorConstants.Logging.OPERATION_RESULT;

@Component
public class WebServiceClient implements IWebServiceClient {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(WebServiceClient.class);
    
    private static final int DEFAULT_CONNECTION_TIMEOUT = 2000;
    private static final int DEFAULT_READ_TIMEOUT = 10000;
    private static final int MAX_CONN_TOTAL = 1000;
    private static final int MAX_CONN_PER_ROUTE = 500;
    private static final String UNKNOWN = "unknown";
    
    @Autowired(required = false)
    IAuthHeaderProducer authHeaderProducer;
    
    @Autowired
    private Serializer jsonSerializer;
    
    private final HttpClient httpClient;
    
    private String baseUri;
    
    public WebServiceClient(String baseUri) {
        this(baseUri, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_READ_TIMEOUT, MAX_CONN_TOTAL, MAX_CONN_PER_ROUTE);
    }
    
    public WebServiceClient(String baseUri, int connectionTimeout, int requestTimeout, int socketTimeout) {
        this(baseUri, connectionTimeout, requestTimeout, socketTimeout, MAX_CONN_TOTAL, MAX_CONN_PER_ROUTE);
    }
    
    public WebServiceClient(String baseUri, int connectionTimeout, int requestTimeout, int socketTimeout, int MaxConnTotal, int MaxConnPerRoute) {
        
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectionTimeout)
                .setConnectionRequestTimeout(requestTimeout)
                .setSocketTimeout(socketTimeout).build();
        
        this.baseUri = baseUri;
        httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setMaxConnTotal(MaxConnTotal)
                .setMaxConnPerRoute(MaxConnPerRoute)
                .build();
    }
    
    @Override
    public <T> T getRequest(Class<T> responseClassType, String endpoint, String acceptedContentType) {
        String url = UrlUtils.buildUrl(baseUri, endpoint);
        HttpGet httpGet = new HttpGet(url);
        try {
            httpGet.addHeader(HttpHeaders.ACCEPT, acceptedContentType);
            httpGet.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
            addAuthHeadersToRequest(httpGet);
            return executeRequest(httpGet, responseClassType);
        } finally {
            httpGet.releaseConnection();
        }
    }
    
    @Override
    public <T> T getRequest(Class<T> responseClassType, String endpoint) {
       return getRequest(responseClassType, endpoint, MediaType.APPLICATION_JSON);
    }
    
    @Override
    public Integer getRequestAsInteger(String endpoint) {
        return getRequest(Integer.class, endpoint, MediaType.TEXT_PLAIN);
    }
    
    private <T> T executeRequest(HttpUriRequest httpRequest, Class<T> responseClassType) {
        long reqTs = System.currentTimeMillis();
        try {
            httpRequest.addHeader(HttpHeaders.USER_AGENT, RedirectorConstants.USER_AGENT_STRING);
            log.setExecutionFlow(ExecutionStep.requestToWS.toString());
            log.info("url=" + httpRequest.getURI());

            return httpClient.execute(httpRequest, (ResponseHandler<T>) response -> {
                int statusCode = response.getStatusLine().getStatusCode();

                Header[] headers = response.getHeaders(HttpHeaders.CONTENT_TYPE);
                String mimeType = UNKNOWN;

                if (headers.length > 0) {
                    mimeType = ContentType.parse(headers[0].getValue()).getMimeType();
                }
                byte[] data = EntityUtils.toByteArray(response.getEntity());
                long duration = System.currentTimeMillis() - reqTs;
                log.setExecutionFlow(ExecutionStep.responseFromWS.toString());

                if (statusCode < HttpStatus.SC_NO_CONTENT) {
                    log.info(OPERATION_RESULT + OperationResult.RequestToWsSuccessful + ", url=" + httpRequest.getURI()
                            + ", method=" + httpRequest.getMethod() + ", status=" + statusCode + ", duration=" + duration);

                    return parseResponseBytes(responseClassType, data, mimeType, httpRequest.getURI().toString());
                }
                String errorMessage = OPERATION_RESULT + OperationResult.RequestToWsFailed + ", url=" + httpRequest.getURI() + ", method=" + httpRequest.getMethod() + ", status=" + statusCode + ", duration=" + duration;
                throw new RestDataSourceExeption(errorMessage);
            });

        } catch (RestDataSourceExeption e) {
            throw  e;
        } catch (IOException e) {
            Metrics.reportRestConnectionIssue(e);

            long duration = System.currentTimeMillis() - reqTs;
            String errorMessage = "url=" + httpRequest.getURI() + " " + OPERATION_RESULT + OperationResult.TimeoutFromWS + " duration=" + duration;
            throw new RestDataSourceExeption(errorMessage, e.getCause());

        } catch (Exception e) {
            Metrics.reportRestConnectionIssue(e);

            long duration = System.currentTimeMillis() - reqTs;
            String errorMessage = "url=" + httpRequest.getURI() + " " + OPERATION_RESULT + OperationResult.RequestToWsFailed + " duration=" + duration;
            throw new RestDataSourceExeption(errorMessage, e.getCause());
        }
    }
    
    private <T> T parseResponseBytes(Class<T> responseClassType, byte[] bytes, String mimeType, String url) {
        log.setExecutionFlow(ExecutionStep.parsingData.toString());
        if (bytes != null && bytes.length > 0) {
            try {
                if (mimeType.equalsIgnoreCase(MediaType.TEXT_PLAIN)) {
                    if (responseClassType.getSimpleName().equals(Integer.class.getSimpleName())) {
                        return (T) Integer.valueOf(new String(bytes));
                    }
                    if (responseClassType.getSimpleName().equals(Boolean.class.getSimpleName())) {
                        return (T) Boolean.valueOf(new String(bytes));
                    }
                } else if (mimeType.equalsIgnoreCase(MediaType.APPLICATION_JSON)) {
                    return jsonSerializer.deserialize(bytes, responseClassType);
                } else if (mimeType.equalsIgnoreCase(MediaType.APPLICATION_XML)){
                    String errorMessage = OPERATION_RESULT + OperationResult.UnsupportedMimeType + ", url=" + url;
                    throw new RestDataSourceExeption(errorMessage);

                } else {
                    String errorMessage = OPERATION_RESULT + OperationResult.UnknownMimeType + ", url=" + url;
                    throw new RestDataSourceExeption(errorMessage);
                }
            } catch (Exception e) {
                String errorMessage = OPERATION_RESULT + OperationResult.ParsingDataError + ", url=" + url;
                throw new RestDataSourceExeption(errorMessage, e.getCause());
            }
        }

        String errorMessage = OPERATION_RESULT + OperationResult.EmptyData + ", url=" + url;
        throw new RestDataSourceExeption(errorMessage);
    }
    
    private void addAuthHeadersToRequest(HttpRequestBase request) {
        if (authHeaderProducer == null) {
            return;
        }
        Map<String, String> authHeaders = authHeaderProducer.getAuthHeaders();
        for (String headerName : authHeaders.keySet()) {
            request.addHeader(headerName, authHeaders.get(headerName));
        }
    }
}