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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */

package com.comcast.redirector.api.redirector;

import com.comcast.redirector.api.RestApiFacade;
import com.comcast.redirector.api.model.StackComment;
import com.comcast.redirector.api.redirector.helpers.HttpTestServerHelper;
import org.junit.Before;
import org.junit.Test;

import static com.comcast.redirector.api.redirector.IntegrationTestUtils.getServiceNameForTest;
import static com.comcast.redirector.api.redirector.IntegrationTestUtils.setupEnv;
import static org.junit.Assert.fail;

public class StackCommentsControllerIntegrationTest {
    private RestApiFacade apiFacade;

    @Before
    public void before() {
        apiFacade = new RestApiFacade(HttpTestServerHelper.BASE_URL);
    }


    @Test
    public void getNonExistentComment_ReturnsEmptyComment() throws Exception {
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);

        StackComment stackComment = new StackComment();
        stackComment.setComment("");

        new StackCommentsServiceSteps(serviceName)
                .getCommentForPath("/PO/POCdonotexists")
                .verifyGetResponseEquals(stackComment);
    }

    @Test
    public void postedComment_isReturnedByGet_AfterPost() throws Exception {
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);
        final String COMMENT = "some comment";
        final String PATH = "/DataCenter1/Region1";
        StackComment stackComment = new StackComment();
        stackComment.setComment(COMMENT);

        new StackCommentsServiceSteps(serviceName)
                .postCommentForPath(stackComment, PATH)
                .verifyPostResponseEquals(stackComment)
                .getCommentForPath(PATH)
                .verifyGetResponseEquals(stackComment);
    }

    @Test
    public void postedComment_IsOverwritten_BySubsequentPost() throws Exception {
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);
        final String COMMENT1 = "some comment";
        final String COMMENT2 = "some comment 2";
        final String PATH = "/DataCenter1/Region1";
        StackComment stackComment1 = new StackComment();
        stackComment1.setComment(COMMENT1);
        StackComment stackComment2 = new StackComment();
        stackComment2.setComment(COMMENT2);

        new StackCommentsServiceSteps(serviceName)
                .postCommentForPath(stackComment1, PATH)
                .verifyPostResponseEquals(stackComment1)
                .getCommentForPath(PATH)
                .verifyGetResponseEquals(stackComment1)
                .postCommentForPath(stackComment2, PATH)
                .verifyPostResponseEquals(stackComment2)
                .getCommentForPath(PATH)
                .verifyGetResponseEquals(stackComment2);
    }

    @Test
    public void post_toOnePath_doesNotAffect_otherPaths() throws Exception {
        String serviceName = getServiceNameForTest();
        setupEnv(serviceName);
        final String COMMENT1 = "some comment";
        final String COMMENT2 = "some comment 2";
        final String PATH1 = "/DataCenter1/Region1";
        final String PATH2 = "/DataCenter2/Region1";
        StackComment stackComment1 = new StackComment();
        stackComment1.setComment(COMMENT1);
        StackComment stackComment2 = new StackComment();
        stackComment2.setComment(COMMENT2);

        new StackCommentsServiceSteps(serviceName)
                .postCommentForPath(stackComment1, PATH1)
                .verifyPostResponseEquals(stackComment1)
                .getCommentForPath(PATH1)
                .verifyGetResponseEquals(stackComment1)
                .postCommentForPath(stackComment2, PATH2)
                .verifyPostResponseEquals(stackComment2)
                .getCommentForPath(PATH2)
                .verifyGetResponseEquals(stackComment2)
                .getCommentForPath(PATH1)
                .verifyGetResponseEquals(stackComment1);
    }

    private class StackCommentsServiceSteps {
        String serviceName;
        StackComment postResponse;
        StackComment getResponse;

        public StackCommentsServiceSteps(String serviceName) {
            this.serviceName = serviceName;
        }

        StackCommentsServiceSteps postCommentForPath (StackComment comment, String path) {
            postResponse = apiFacade.postStackCommentForPath(comment, serviceName, path);

            return this;
        }

        StackCommentsServiceSteps getCommentForPath (String path) {
            getResponse = apiFacade.getStackCommentForPath(serviceName, path);

            return this;
        }

        StackCommentsServiceSteps verifyPostResponseEquals (StackComment toCompare) {
            if (!toCompare.equals(postResponse)) {
                fail();
            }

            return this;
        }

         StackCommentsServiceSteps verifyGetResponseEquals (StackComment toCompare) {
            if (!toCompare.equals(getResponse)) {
                fail();
            }

            return this;
        }
    }
}
