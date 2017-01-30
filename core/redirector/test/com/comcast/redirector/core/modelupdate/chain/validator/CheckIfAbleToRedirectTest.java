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

package com.comcast.redirector.core.modelupdate.chain.validator;

import com.comcast.redirector.ruleengine.model.Model;
import com.comcast.redirector.common.InstanceInfo;
import com.comcast.redirector.core.balancer.serviceprovider.IServiceProviderManager;
import com.comcast.redirector.core.balancer.serviceprovider.whitelist.WhiteList;
import com.comcast.redirector.core.engine.IRedirectorEngine;
import com.comcast.redirector.core.engine.IRedirectorEngineFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CheckIfAbleToRedirectTest {
    private static final String APP_NAME = "APP_NAME";
    private static final int MIN_HOSTS = 10;

    private IServiceProviderManager serviceProviderManager;
    private IRedirectorEngineFactory factory;
    private Model flavorRulesModel;
    private IRedirectorEngine.IHostSelector hostSelector;
    private CheckIfAbleToRedirect validator;

    @Before
    public void setUp() throws Exception {
        flavorRulesModel = mock(Model.class);
        factory = mock(IRedirectorEngineFactory.class);
        serviceProviderManager = mock(IServiceProviderManager.class);
        hostSelector = mock(IRedirectorEngine.IHostSelector.class);

        when(factory.newHostSelector(anyString(), eq(serviceProviderManager), any(WhiteList.class), any(Model.class)))
                .thenReturn(hostSelector);

        when(hostSelector.getDefaultHost()).thenReturn(mock(InstanceInfo.class));
    }

    private CheckIfAbleToRedirect createSimpleMathValidator() {
        return new CheckIfAbleToRedirect.Builder()
                .setAppName(APP_NAME)
                .setFlavorRulesModel(flavorRulesModel)
                .setServiceProviderManager(serviceProviderManager)
                .setRedirectorEngineFactory(factory)
                .setMinHosts(MIN_HOSTS)
                .build();
    }

    @Test
    public void testCantRedirectToDefault() throws Exception {
        validator = createSimpleMathValidator();
        when(hostSelector.getDefaultHost()).thenReturn(null);

        ValidationReport report = validator.validate();
        Assert.assertEquals(Validator.ValidationResultType.CANT_REDIRECT_TO_DEFAULT, report.getValidationResultType());
    }

    @Test
    public void testCantRedirectBecauseNotEnoughHostsForDefault() throws Exception {
        validator = createSimpleMathValidator();

        ValidationReport report = validator.validate();
        Assert.assertEquals(Validator.ValidationResultType.NOT_ENOUGH_HOSTS_FOR_DEFAULT, report.getValidationResultType());
    }

    @Test
    public void testSimpleMathSuccessManyDefault() throws Exception {
        validator = createSimpleMathValidator();
        when(hostSelector.getCountOfHostsForDefaultServer()).thenReturn(MIN_HOSTS);
        when(hostSelector.getCountOfHostsForDistribution()).thenReturn(0);

        ValidationReport report = validator.validate();
        Assert.assertEquals(Validator.ValidationResultType.SUCCESS, report.getValidationResultType());
    }

    @Test
    public void testSimpleMathSuccessManyDistribution() throws Exception {
        validator = createSimpleMathValidator();
        when(hostSelector.getCountOfHostsForDefaultServer()).thenReturn(1);
        when(hostSelector.getCountOfHostsForDistribution()).thenReturn(MIN_HOSTS - 1);

        ValidationReport report = validator.validate();
        Assert.assertEquals(Validator.ValidationResultType.SUCCESS, report.getValidationResultType());
    }

    @Test
    public void testSimpleMathFailureNotEnoughForDefault() throws Exception {
        validator = createSimpleMathValidator();
        when(hostSelector.getCountOfHostsForDefaultServer()).thenReturn(MIN_HOSTS - 1);
        when(hostSelector.getCountOfHostsForDistribution()).thenReturn(0);

        ValidationReport report = validator.validate();
        Assert.assertEquals(Validator.ValidationResultType.NOT_ENOUGH_HOSTS_FOR_DEFAULT, report.getValidationResultType());
    }

    @Test
    public void testSimpleMathSuccessGoingToDefault() throws Exception {
        validator = createSimpleMathValidator();
        when(hostSelector.getCountOfHostsForDefaultServer()).thenReturn(MIN_HOSTS);

        ValidationReport report = validator.validate();
        Assert.assertEquals(Validator.ValidationResultType.SUCCESS, report.getValidationResultType());
    }

    @Ignore("Deviation feature has been disabled. Tests are disabled as well")
    @Test
    public void testSimpleMathFailureTooMuchDeviation() throws Exception {
        //Test case if deviation equals threshold
        validator = createSimpleMathValidator();
        when(hostSelector.getCountOfHostsForDefaultServer()).thenReturn(MIN_HOSTS);
        when(hostSelector.getPercentDeviationCountOfHostsForDistribution()).thenReturn(40);

        ValidationReport report = validator.validate();
        Assert.assertEquals(Validator.ValidationResultType.TOO_MUCH_DEVIATION, report.getValidationResultType());

        //Test case if deviation equals 100%
        validator = createSimpleMathValidator();
        when(hostSelector.getCountOfHostsForDefaultServer()).thenReturn(MIN_HOSTS);
        when(hostSelector.getPercentDeviationCountOfHostsForDistribution()).thenReturn(100);

        report = validator.validate();
        Assert.assertEquals(Validator.ValidationResultType.TOO_MUCH_DEVIATION, report.getValidationResultType());
    }

    @Ignore("Deviation feature has been disabled. Tests are disabled as well")
    @Test
    public void testSimpleMathSuccessIfDeviationLessThanThreshold() throws Exception {
        //Test case if deviation is 1%
        validator = createSimpleMathValidator();
        when(hostSelector.getCountOfHostsForDefaultServer()).thenReturn(MIN_HOSTS);
        when(hostSelector.getPercentDeviationCountOfHostsForDistribution()).thenReturn(1);

        ValidationReport report = validator.validate();
        Assert.assertEquals(Validator.ValidationResultType.ALLOWABLE_DEVIATION, report.getValidationResultType());

        //Test case if deviation is 39%
        validator = createSimpleMathValidator();
        when(hostSelector.getCountOfHostsForDefaultServer()).thenReturn(MIN_HOSTS);
        when(hostSelector.getPercentDeviationCountOfHostsForDistribution()).thenReturn(39);

        report = validator.validate();
        Assert.assertEquals(Validator.ValidationResultType.ALLOWABLE_DEVIATION, report.getValidationResultType());
    }

    @Test
    public void testSimpleMathSuccessIfDeviationIsZero() throws Exception {
        validator = createSimpleMathValidator();
        when(hostSelector.getCountOfHostsForDefaultServer()).thenReturn(MIN_HOSTS);
        when(hostSelector.getPercentDeviationCountOfHostsForDistribution()).thenReturn(0);

        ValidationReport report = validator.validate();
        Assert.assertEquals(Validator.ValidationResultType.SUCCESS, report.getValidationResultType());
    }
}
