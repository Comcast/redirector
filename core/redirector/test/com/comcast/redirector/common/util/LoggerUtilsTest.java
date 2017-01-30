package com.comcast.redirector.common.util;

import com.comcast.redirector.common.Constants;
import com.comcast.redirector.common.InstanceInfo;
import com.comcast.redirector.ruleengine.model.Server;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LoggerUtilsTest {
    private Map<String, String> context;
    private InstanceInfo instanceInfo;
    
    @Before
    public void setUp() {
        context = createContext("serviceAccessToken");
        instanceInfo = mock(InstanceInfo.class);
    }
    
    @Test
    public void testParameterContext() {
        String redirectLog = LoggerUtils.getRedirectLog(context, instanceInfo);
        
        Assert.assertTrue(redirectLog.contains("serviceAccountId=AccountID"));
        Assert.assertTrue(redirectLog.contains("mac=00:AA:BB:CC:DD:EE:FF:00"));
        Assert.assertTrue(redirectLog.contains("clientAddress=192.168.155.49"));
        
        Assert.assertTrue(redirectLog.contains("serviceAccessToken=123456789ABC"));
        Assert.assertTrue(redirectLog.contains("language=en"));
    }
    
    @Test
    public void testParameterContextAndServiceAccessTokenIsRemoved() {
        context.remove("serviceAccessToken");
        String redirectLog = LoggerUtils.getRedirectLog(context, instanceInfo);
        
        Assert.assertTrue(redirectLog.contains("serviceAccountId=AccountID"));
        Assert.assertTrue(redirectLog.contains("mac=00:AA:BB:CC:DD:EE:FF:00"));
        Assert.assertTrue(redirectLog.contains("clientAddress=192.168.155.49"));
        
        Assert.assertFalse(redirectLog.contains("serviceAccessToken=123456789ABC"));
        Assert.assertTrue(redirectLog.contains("language=en"));
    }
    
    @Test
    public void testParameterContextIfDefaulRulePresent() {
        when(instanceInfo.getRuleName()).thenReturn("default");
        when(instanceInfo.getAppliedUrlRules()).thenReturn(Collections.singleton("default"));
        when(instanceInfo.getServer()).thenReturn(mock(Server.class));
        
        String redirectLog = LoggerUtils.getRedirectLog(context, instanceInfo);
        
        Assert.assertTrue(redirectLog.contains("mac=00:AA:BB:CC:DD:EE:FF:00"));
        Assert.assertTrue(redirectLog.contains("serviceAccountId=AccountID"));
        Assert.assertTrue(redirectLog.contains("clientAddress=192.168.155.49"));
        
        Assert.assertFalse(redirectLog.contains("serviceAccessToken=123456789ABC"));
        Assert.assertFalse(redirectLog.contains("language=en"));
        
    }
    
    @Test
    public void testParameterContextIfDefaulRulePresentAndServiceAccessTokenIsRemoved() {
        context.remove("serviceAccessToken");
        when(instanceInfo.getRuleName()).thenReturn("default");
        when(instanceInfo.getAppliedUrlRules()).thenReturn(Collections.singleton("default"));
        when(instanceInfo.getServer()).thenReturn(mock(Server.class));
        
        String redirectLog = LoggerUtils.getRedirectLog(context, instanceInfo);
        
        Assert.assertTrue(redirectLog.contains("mac=00:AA:BB:CC:DD:EE:FF:00"));
        Assert.assertTrue(redirectLog.contains("serviceAccountId=AccountID"));
        Assert.assertTrue(redirectLog.contains("clientAddress=192.168.155.49"));
        
        Assert.assertFalse(redirectLog.contains("serviceAccessToken=123456789ABC"));
        Assert.assertFalse(redirectLog.contains("language=en"));
        
    }
    
    private Map<String, String> createContext(String serviceAccessToken) {
        Map<String, String> context = new HashMap<>();
        
        context.put(Constants.RECEIVER_ID, "P01275916003");
        context.put("language", "en");
        context.put("deviceId", "000000000001");
        context.put("clientAddress", "192.168.155.49");
        context.put(serviceAccessToken, "eyJra.kiOiI2YzgzYjUxNi0zOTQ1LTQ3NzctYTM5NS0wZjY1ZjQxNzE0ZTYiLCJpc3MiOiJzYXRzLXByb2R1Y3Rpb24iLCJzdWIiOiJ4Ym8tYm93cyIsImlhdCI6MTQ3NDQyMjQ1OSwibmJmIjoxNDc0NDIyNDU5LCJleHAiOjE0NzU3MTg0NjIsInZlcnNpb24iOiIxLjAiLCJhbGxvd2VkUmVzb3VyY2VzIjp7ImFsbG93ZWREZXZpY2VJZHMiOlsiMjI4MDU0ODQxNjYxOTEzMDkyOSJdLCJhbGxvd2VkUGFydG5lcnMiOlsiQ29tY2FzdCJdLCJhbGxvd2VkU2VydmljZUFjY291bnRJZHMiOlsiODg1Mzk4NzYzMjAxMTY1MzY0NyJdLCJhbGxvd2VkVXNlcklkcyI6WyI5MTM5NDcwMTgxNDg4NTY3NjciXX0sImNhcGFiaWxpdGllcyI6W10sImF1ZCI6W119.CvyjlJtIhbrxQ90WF0bpUUH1UwefXD5SHtqHC6sGL4LVD0hvDxAvHEXZ3v0G1gd9iOT9fNk_FJKBqc1DtZdHvtj5Onljw20wsf7QIZKN123456789ABC");
        context.put("model", "AX013AN");
        context.put(Constants.SERVICE_ACCOUNT_ID, "AccountID");
        context.put(Constants.MAC, "00:AA:BB:CC:DD:EE:FF:00");
        return context;
    }
}
