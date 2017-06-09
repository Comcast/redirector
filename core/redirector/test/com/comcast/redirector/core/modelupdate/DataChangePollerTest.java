/*
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
 * @author Alexander Ievstratiev (oievstratiev@productengine.com)
 */

package com.comcast.redirector.core.modelupdate;

import com.comcast.redirector.webserviceclient.IWebServiceClient;
import org.glassfish.jersey.internal.util.Producer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class DataChangePollerTest {

    private static final int INTERVAL = 1;
    private IWebServiceClient webServiceClient;
    private DataChangePoller dataChangePoller;
    private NewVersionHandler<Integer> newVersionHandler;
    private Producer<Integer> getCurrentVersionProducer;
    private Consumer<Integer> secCurrentVersionConsumer;

    @Before
    public void setUp() throws Exception {
        webServiceClient = setupWebServiceClient();

        newVersionHandler = setupNewVersionHandler();
        getCurrentVersionProducer = setupProducer();
        secCurrentVersionConsumer = setupConsumer();

        dataChangePoller = new DataChangePoller(webServiceClient);
    }

    @After
    public void tearDown() throws Exception {
        dataChangePoller.shutdown();
    }

    @Test
    public void testDataChangePolling_ThenVerionsAreNotEqual() throws Exception {
        setupExpectedResultForWebServiceClient(new Integer(10));
        setupExpectedResultForGetCurrentVersionProducer(new Integer(9));

        dataChangePoller.startDataChangePolling("refreshUnitTest", "/unitTest",
                INTERVAL, getNewVersionHandler(), getCurrentVersionProducer, secCurrentVersionConsumer, null, null);

        verifyResultThenVersionsAreNotEqual();
    }

    @Test
    public void testDataChangePolling_ThenVerionsAreEqual() throws Exception {
        setupExpectedResultForWebServiceClient(new Integer(10));
        setupExpectedResultForGetCurrentVersionProducer(new Integer(10));

        dataChangePoller.startDataChangePolling("refreshUnitTest", "/unitTest",
                INTERVAL, newVersionHandler, getCurrentVersionProducer, secCurrentVersionConsumer, null, null);

        verifyResultThenVersionsAreEqual();
    }

    private void verifyResultThenVersionsAreNotEqual() throws InterruptedException {
        TimeUnit.SECONDS.sleep(2);
        verify(secCurrentVersionConsumer, atLeast(1)).accept(10);
    }

    private void verifyResultThenVersionsAreEqual() throws InterruptedException {
        TimeUnit.SECONDS.sleep(2);
        verify(secCurrentVersionConsumer, never()).accept(10);
    }

    private IWebServiceClient setupWebServiceClient() {
        return mock(IWebServiceClient.class);
    }

    private <T> void setupExpectedResultForWebServiceClient(Integer result) {
        when(webServiceClient.getRequestAsInteger(anyString())).thenReturn(result);
    }

    private void setupExpectedResultForGetCurrentVersionProducer(Integer result) {
        when(getCurrentVersionProducer.call()).thenReturn(result);
    }

    private Producer setupProducer() {
        return mock(Producer.class);
    }

    private NewVersionHandler setupNewVersionHandler() {
        return mock(NewVersionHandler.class);
    }

    private NewVersionHandler getNewVersionHandler() {
        return (newVersion, updateCurrentVersion) -> updateCurrentVersion.accept(newVersion);
    }

    private Consumer setupConsumer() {
        return mock(Consumer.class);
    }


}