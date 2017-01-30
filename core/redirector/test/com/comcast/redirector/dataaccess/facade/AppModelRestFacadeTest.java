package com.comcast.redirector.dataaccess.facade;

import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.SelectServer;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.url.rule.UrlRule;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.core.config.ZKConfig;
import com.comcast.redirector.core.modelupdate.IDataChangePoller;
import com.comcast.redirector.dataaccess.client.DataSourceConnectorException;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.dao.IDAOFactory;
import com.comcast.redirector.webserviceclient.IWebServiceClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class AppModelRestFacadeTest {
    private static final String BASE_PATH = "/BASE_PATH";
    private static final String SERVICE_NAME = "TEST";
    
    private IDataSourceConnector connector;
    private IDAOFactory daoFactory;
    private IDataChangePoller dataChangePoller;
    private IWebServiceClient webServiceClient;
    private AppModelRestFacade testee;

    private ZKConfig config;

    @Before
    public void setUp() throws Exception {
        setupConnector();
        webServiceClient = setupWebServiceClient();
        dataChangePoller = setupDataChangePoller();
        config = mock(ZKConfig.class);
        when(config.getRestBasePath()).thenReturn("localhost:10540/");
        when(config.getStacksPollIntervalSeconds()).thenReturn(2);
        when(config.getModelPollIntervalSeconds()).thenReturn(2);
    }
    
    @Test
    public void testStartPerServiceWhenConnected() throws Exception {
        setupSuccessfullyConnectingPerServiceCache(true);
        testee.start();
        Assert.assertTrue(testee.isAvailable());
    }
    
    @Test
    public void testStartPerServiceWhenNotConnected() throws Exception {
        setupSuccessfullyConnectingPerServiceCache(false);
        testee.start();
        Assert.assertFalse(testee.isAvailable());
    }
    
    @Test
    public void testGetServerNotNull() throws Exception {
        Server server = new Server();
        setupSuccessfullyConnectingPerServiceCache(true);
        setupExpectedResultForWebServiceClient(server);
        
        Assert.assertEquals(server, testee.getServer("test"));
    }
    
    @Test
    public void testGetServerNodeDataNull() throws Exception {
        setupSuccessfullyConnectingPerServiceCache(true);
        setupExpectedResultForWebServiceClient(null);
        
        Assert.assertNull(testee.getServer("test"));
    }
    
    @Test
    public void testGetUrlParamsNodeDataNotNull() throws Exception {
        setupSuccessfullyConnectingPerServiceCache(true);
        UrlRule urlParams = new UrlRule();
        setupExpectedResultForWebServiceClient(urlParams);
        
        Assert.assertEquals(urlParams, testee.getUrlParams("test"));
    }
    
    @Test
    public void testGetUrlParamsNodeDataNull() throws Exception {
        setupSuccessfullyConnectingPerServiceCache(true);
        setupExpectedResultForWebServiceClient(null);
        
        Assert.assertNull(testee.getUrlParams("test"));
    }
    
    @Test
    public void testGetDistributionNotNull() throws Exception {
        setupSuccessfullyConnectingPerServiceCache(true);
        Distribution distribution = new Distribution();
        setupExpectedResultForWebServiceClient(distribution);
        
        Assert.assertEquals(distribution, testee.getDistribution());
    }
    
    @Test
    public void testGetDistributionNull() throws Exception {
        setupSuccessfullyConnectingPerServiceCache(true);
        setupExpectedResultForWebServiceClient(null);
        
        Assert.assertNull(testee.getDistribution());
    }
    
    @Test
    public void testGetUrlRuleNotNull() throws Exception {
        setupSuccessfullyConnectingPerServiceCache(true);
        IfExpression rule = new IfExpression();
        setupExpectedResultForWebServiceClient(rule);
        
        Assert.assertEquals(rule, testee.getUrlRule("test"));
    }
    
    @Test
    public void testGetUrlRuleNull() throws Exception {
        setupSuccessfullyConnectingPerServiceCache(true);
        setupExpectedResultForWebServiceClient(null);
        
        Assert.assertNull(testee.getUrlRule("test"));
    }
    
    @Test
    public void testGetUrlRulesNotNull() throws Exception {
        setupSuccessfullyConnectingPerServiceCache(true);
        URLRules uRLRules = new URLRules();
        List<IfExpression> rules = new ArrayList<>();
        rules.add(new IfExpression());
        uRLRules.setItems(rules);
        setupExpectedResultForWebServiceClient(uRLRules);
        
        Assert.assertEquals(rules, testee.getUrlRules());
    }
    
    @Test
    public void testGetFlavorRulesMap() throws Exception {
        setupSuccessfullyConnectingPerServiceCache(true);
        SelectServer select = new SelectServer();
        List<IfExpression> rules = new ArrayList<>();
        rules.add(new IfExpression());
        select.setItems(rules);
        setupExpectedResultForWebServiceClient(select);
        
        Assert.assertEquals(rules, testee.getFlavorRules());
    }
    
    @Test
    public void testGetWhitelistedStacksNotNull() throws Exception {
        setupSuccessfullyConnectingPerServiceCache(true);
        Whitelisted whitelist = new Whitelisted();
        setupExpectedResultForWebServiceClient(whitelist);
        
        Assert.assertEquals(whitelist, testee.getWhitelist());
    }
    
    @Test
    public void testGetWhitelistedStacksNodeDataNull() throws Exception {
        setupSuccessfullyConnectingPerServiceCache(true);
        setupExpectedResultForWebServiceClient(null);
        
        Assert.assertNull(testee.getWhitelist());
    }

    @Ignore("Do we still need it? We don't get this version from Zk")
    @Test
    public void testGetModelVersion() throws Exception {
        int version = 5;
        setupSuccessfullyConnectingPerServiceCache(true);
        setupGetVersionReturns(version);
        testee.start();
        
        Assert.assertEquals(version, testee.getModelVersion());
    }

    @Ignore("Do we still need it? We don't get this version from Zk")
    @Test
    public void testGetModelVersionStatsNull() throws Exception {
        setupSuccessfullyConnectingPerServiceCache(true);
        setupGetVersionThrowsException();
        testee.start();
        
        Assert.assertEquals(RedirectorConstants.NO_MODEL_NODE_VERSION, testee.getModelVersion());
    }

    @Ignore("Do we still need it? We don't get this version from Zk")
    @Test
    public void testGetModelVersionCacheNotAvailable() throws Exception {
        setupSuccessfullyConnectingPerServiceCache(true);
        
        Assert.assertEquals(RedirectorConstants.NO_MODEL_NODE_VERSION, testee.getModelVersion());
    }
    
    private IWebServiceClient setupWebServiceClient() {
        return mock(IWebServiceClient.class);
    }
    
    private IDataChangePoller setupDataChangePoller() {
        return mock(IDataChangePoller.class);
    }
    
    private void setupConnector() {
        connector = mock(IDataSourceConnector.class);
        when(connector.getBasePath()).thenReturn(BASE_PATH);
    }
    
    private void setupSuccessfullyConnectingPerServiceCache(boolean isConnected) throws InterruptedException {
        setupConnector(isConnected);
        testee = new AppModelRestFacade(connector, SERVICE_NAME, dataChangePoller, webServiceClient, config);
    }
    
    private void setupConnector(boolean isConnected) throws InterruptedException {
        when(connector.blockUntilConnectedOrTimedOut()).thenReturn(isConnected);
    }
    
    private <T> void setupExpectedResultForWebServiceClient(T result) {
        when(webServiceClient.getRequest(any(), anyString())).thenReturn(result);
        when(webServiceClient.getRequest(any(), anyString())).thenReturn(result);
    }
    
    private void setupGetVersionReturns(int version) throws DataSourceConnectorException {
        when(connector.getNodeVersion(anyString())).thenReturn(version);
    }
    
    private void setupGetVersionThrowsException() throws DataSourceConnectorException {
        doThrow(new DataSourceConnectorException()).when(connector).getNodeVersion(anyString());
    }
}
