package com.comcast.redirector.webserviceclient;

public interface IWebServiceClient {

     <T> T getRequest(Class<T> responseClassType, String endpoint);
     
     <T> T getRequest(Class<T> responseClassType, String endpoint, String acceptedMediaType);
     
     Integer getRequestAsInteger(String endpoint);
}
