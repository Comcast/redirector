/**
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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */

package com.comcast.redirector.api.redirector.negativeintegrationtests;

import com.comcast.redirector.api.model.builders.DistributionBuilder;
import com.comcast.redirector.api.model.distribution.Distribution;
import com.comcast.redirector.api.model.validation.ValidationState;
import com.comcast.redirector.api.redirector.helpers.DistributionHelper;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import com.comcast.redirector.api.redirector.helpers.ServiceHelper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import static com.comcast.redirector.api.model.factory.DistributionRuleFactory.newDistributionRule;
import static com.comcast.redirector.api.model.factory.ServerFactory.newServerAdvanced;
import static com.comcast.redirector.api.model.factory.ServerFactory.newSimpleServerForFlavor;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class DistributionNegativeIntegrationTests extends BaseNegativeTests{

    @Test
    public void testDistributionPercentageInvalidError() {
        Distribution distribution = new DistributionBuilder().
                withDefaultServer(defaultServer).
                withRule(newDistributionRule(1, 101f, newSimpleServerForFlavor("anyFlavor"))).
                build();

        ValidationState error = postDistributionWithValidation(distribution);

        Assert.assertEquals(2, error.getErrors().size());
        Assert.assertTrue(error.getErrors().containsKey(ValidationState.ErrorType.DistributionPercentageInvalid));
        Assert.assertTrue(error.getErrors().containsKey(ValidationState.ErrorType.RuleDistributionPercentageInvalid));
    }

    @Test
    public void testDistributionDuplicatedServersError() {
        Distribution distribution = new DistributionBuilder().
                withDefaultServer(defaultServer).
                withRule(newDistributionRule(1, 5f, newSimpleServerForFlavor("anySameFlavor"))).
                withRule(newDistributionRule(2, 5f, newSimpleServerForFlavor("anySameFlavor"))).
                build();

        ValidationState error = postDistributionWithValidation(distribution);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.DistributionDuplicatedServersErr, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testDistributionOnlySimpleServersError() {
        Distribution distribution = new DistributionBuilder().
                withDefaultServer(defaultServer).
                withRule(newDistributionRule(1, 5f, newSimpleServerForFlavor("anyFlavor"))).
                withRule(newDistributionRule(2, 5f, newServerAdvanced("xre://tst"))).
                build();

        ValidationState error = postDistributionWithValidation(distribution);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.DistributionOnlySimpleServersErr, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testDistributionDeletionError() {
        Distribution validDistributionWithSeveralRules = new DistributionBuilder().
                withDefaultServer(newSimpleServerForFlavor("DefaultServerFlavor")).
                withRule(newDistributionRule(1, 5f, newSimpleServerForFlavor("AnyDistributionFlavor_1"))).
                withRule(newDistributionRule(2, 5f, newSimpleServerForFlavor("AnyDistributionFlavor_2"))).
                withRule(newDistributionRule(3, 5f, newSimpleServerForFlavor("AnyDistributionFlavor_3"))).
                build();

        ValidationState noErrorsOnPost = postDistributionWithValidation(validDistributionWithSeveralRules);
        Assert.assertEquals(0, noErrorsOnPost.getErrors().size());

        DistributionHelper.approvePendingChanges(HttpTestServerHelper.target(), SERVICE_NAME);

        // now try to remove all rules from
        Distribution emptyDistribution = new DistributionBuilder()
            .withDefaultServer(newSimpleServerForFlavor("DefaultServerFlavor")).build();
        ValidationState error = postDistributionWithValidation(emptyDistribution);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.DistributionDeletionError, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testRuleDistributionPercentageInvalidError() {
        Distribution distributionWithNegativePercents = new DistributionBuilder().
                withDefaultServer(defaultServer).
                withRule(newDistributionRule(1, -1f, newSimpleServerForFlavor("someFlavor"))).
                withRule(newDistributionRule(2, 1f, newSimpleServerForFlavor("someOtherFlavor"))).
                build();

        ValidationState error = postDistributionWithValidation(distributionWithNegativePercents);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.RuleDistributionPercentageInvalid, error.getErrors().keySet().iterator().next());
    }

    @Test
    public void testServerPathOverridesDistributionError() {
        Distribution distribution = new DistributionBuilder().
                withDefaultServer(newSimpleServerForFlavor("SameFlavorInDefaultAndDistribution")).
                withRule(newDistributionRule(1, 5f, newSimpleServerForFlavor("anyFlavor"))).
                withRule(newDistributionRule(2, 7f, newSimpleServerForFlavor("anyOtherFlavor"))).
                withRule(newDistributionRule(3, 9f, newSimpleServerForFlavor("SameFlavorInDefaultAndDistribution"))).
                build();

        ValidationState error = postDistributionWithValidation(distribution);

        Assert.assertEquals(1, error.getErrors().size());
        Assert.assertEquals(ValidationState.ErrorType.ServerPathOverridesDistribution, error.getErrors().keySet().iterator().next());
    }

    private ValidationState postDistributionWithValidation(Distribution distribution) {
        WebTarget webTarget = HttpTestServerHelper.target().path(DISTRIBUTION_SERVICE_PATH).path(SERVICE_NAME);
        return ServiceHelper.post(webTarget, distribution, MediaType.APPLICATION_JSON, ValidationState.class);
    }
}
