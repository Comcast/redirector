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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.xre.common.redirector.v2.utils;

import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.PathHelper;
import com.comcast.tvx.cloud.MetaData;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class HostRegistrationIntegrationTest {
    private String connectionString = "localhost:21821";
    private String zookeeperBasePath = "/hostsRegistration";
    private CuratorFramework curatorFramework;
    private ServiceDiscovery<MetaData> serviceDiscovery;

    @Before
    public void setUp() throws Exception {
        startCurator(connectionString);
        startServiceDiscovery(zookeeperBasePath);
    }

    @After
    public void tearDown() throws Exception {
        curatorFramework.close();
    }

    private void startCurator(String connectionString) {
        curatorFramework = CuratorFrameworkFactory.builder()
            .connectionTimeoutMs(10 * 1000)
            .retryPolicy(new ExponentialBackoffRetry(10, 20))
            .connectString(connectionString).build();

        curatorFramework.start();
    }

    private void startServiceDiscovery(String zookeeperBasePath) throws Exception {
        serviceDiscovery = ServiceDiscoveryBuilder.builder(MetaData.class)
            .client(curatorFramework)
            .basePath(PathHelper.getPathHelper(EntityType.STACK, zookeeperBasePath).getPath())
            .build();
        serviceDiscovery.start();
    }

    @Test
    public void toolRegistersGivenNumberOfHosts_And_Exits_AfterTTLExpiration() throws Exception {
        int ttlInMinutes = 1;
        int numberOfInstances = 5;
        String[] args = ("-connection " + connectionString + " -zkBasePath " + zookeeperBasePath +
            " -stack /test/test1 -flavor TEST-FLAVOR1 -app testAppName -hosts " + numberOfInstances +
            " -weight 3 -ip 10.10.10.1 -ipv6 2001::eef1 -ttl " + ttlInMinutes)
            .split(" ");

        CountDownLatch ttlExpired = new CountDownLatch(1);
        Thread hostRegistrationThread = new Thread(() -> {
            new HostRegistration(args).start();
            ttlExpired.countDown();
        });
        hostRegistrationThread.setDaemon(true);
        hostRegistrationThread.start();
        TimeUnit.SECONDS.sleep(5);

        ServiceProvider<MetaData> serviceProvider = serviceDiscovery.serviceProviderBuilder().serviceName("/test/test1/TEST-FLAVOR1/testAppName").build();
        serviceProvider.start();

        Set<String> instances = serviceProvider.getAllInstances().stream()
            .map(input -> input.getPayload().getParameters().get("ipv4Address"))
            .collect(Collectors.toSet());

        Set<String> instancesIpv6 = serviceProvider.getAllInstances().stream()
                .map(input -> input.getPayload().getParameters().get("ipv6Address"))
                .collect(Collectors.toSet());

        Assert.assertEquals(numberOfInstances, instances.size());

        Assert.assertTrue(instances.contains("10.10.10.1"));
        Assert.assertTrue(instances.contains("10.10.10.2"));
        Assert.assertTrue(instances.contains("10.10.10.3"));
        Assert.assertTrue(instances.contains("10.10.10.4"));
        Assert.assertTrue(instances.contains("10.10.10.5"));

        ttlExpired.await(60, TimeUnit.SECONDS);
        TimeUnit.SECONDS.sleep(5); /* margin */

        Assert.assertTrue(serviceProvider.getAllInstances().isEmpty());
    }

    @Test
    public void reject_MissingConnectionInArguments() {
        String missingConnectionArgs = "-stack /test/test1 -flavor TEST-FLAVOR1 -app testAppName -hosts 5 -weight 3 -ip 10.10.10.1";

        verifyIncorrectArgumentsRejected(missingConnectionArgs);
    }

    @Test
    public void reject_InvalidConnectionArguments() {
        String invalidConnectionArgs = "-connection invalid -stack /test/test1 -flavor TEST-FLAVOR1 -app testAppName -hosts 5 -weight 3 -ip 10.10.10.1";

        verifyIncorrectArgumentsRejected(invalidConnectionArgs);
    }

    @Test
    public void reject_InvalidZookeeperBasePathArguments() {
        String invalidConnectionArgs = "-connection 127.0.0.1:1000 -zkBasePath invalid -stack /test/test1 -flavor TEST-FLAVOR1 -app testAppName -hosts 5 -weight 3 -ip 10.10.10.1";

        verifyIncorrectArgumentsRejected(invalidConnectionArgs);
    }

    @Test
    public void reject_MissingStackArguments() {
        String missingStackArgs = "-connection " + connectionString + " -flavor TEST-FLAVOR1 -app testAppName -hosts 5 -weight 3 -ip 10.10.10.1";

        verifyIncorrectArgumentsRejected(missingStackArgs);
    }

    @Test
    public void reject_MissingFlavorArguments() {
        String missingFlavorArgs = "-connection " + connectionString + " -stack /test/test1 -app testAppName -hosts 5 -weight 3 -ip 10.10.10.1";

        verifyIncorrectArgumentsRejected(missingFlavorArgs);
    }

    @Test
    public void reject_MissingAppArguments() {
        String missingAppArgs = "-connection " + connectionString + " -stack /test/test1 -flavor TEST-FLAVOR1 -hosts 5 -weight 3 -ip 10.10.10.1";

        verifyIncorrectArgumentsRejected(missingAppArgs);
    }

    @Test
    public void reject_MissingHostArguments() {
        String missingHostsArgs = "-connection " + connectionString + " -stack /test/test1 -flavor TEST-FLAVOR1 -app testAppName -weight 3 -ip 10.10.10.1";

        verifyIncorrectArgumentsRejected(missingHostsArgs);
    }

    @Test
    public void reject_NegativeHostArguments() {
        String missingHostsArgs = "-connection " + connectionString + " -stack /test/test1 -flavor TEST-FLAVOR1 -app testAppName -hosts -100 -weight 3 -ip 10.10.10.1";

        verifyIncorrectArgumentsRejected(missingHostsArgs);
    }

    @Test
    public void reject_ZeroHostArguments() {
        String missingHostsArgs = "-connection " + connectionString + " -stack /test/test1 -flavor TEST-FLAVOR1 -app testAppName -hosts 0 -weight 3 -ip 10.10.10.1";

        verifyIncorrectArgumentsRejected(missingHostsArgs);
    }

    @Test
    public void reject_MissingWeightArguments() {
        String missingWeightArgs = "-connection " + connectionString + " -stack /test/test1 -flavor TEST-FLAVOR1 -app testAppName -hosts 5 -ip 10.10.10.1";

        verifyIncorrectArgumentsRejected(missingWeightArgs);
    }

    @Test
    public void reject_MissingIpArguments() {
        String missingIpArgs = "-connection " + connectionString + " -stack /test/test1 -flavor TEST-FLAVOR1 -app testAppName -weight 3 -hosts 5";

        verifyIncorrectArgumentsRejected(missingIpArgs);
    }

    private void verifyIncorrectArgumentsRejected(String args) {
        try {
            new HostRegistration(args.split(" "));
        } catch (HostRegistration.ProgramFailedException e) {
            return;
        }

        Assert.fail("Tool should fail on incorrect arguments " + args);
    }

    private void verifyIpv6 (Set<String> instancesIpv6, int numberOfInstances) {

        Assert.assertEquals(numberOfInstances, instancesIpv6.size());
        Assert.assertTrue(instancesIpv6.contains("2001:0000:0000:0000:0000:0000:0000:eef1"));
        Assert.assertTrue(instancesIpv6.contains("2001:0000:0000:0000:0000:0000:0000:eef2"));
        Assert.assertTrue(instancesIpv6.contains("2001:0000:0000:0000:0000:0000:0000:eef3"));
        Assert.assertTrue(instancesIpv6.contains("2001:0000:0000:0000:0000:0000:0000:eef4"));
        Assert.assertTrue(instancesIpv6.contains("2001:0000:0000:0000:0000:0000:0000:eef5"));
    }
}
