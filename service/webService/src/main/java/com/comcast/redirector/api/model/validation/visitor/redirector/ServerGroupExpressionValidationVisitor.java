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

package com.comcast.redirector.api.model.validation.visitor.redirector;

import com.comcast.redirector.api.model.ExpressionVisitor;
import com.comcast.redirector.api.model.IVisitable;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.api.model.ServerGroup;
import com.comcast.redirector.api.model.validation.BaseExpressionValidationVisitor;
import com.comcast.redirector.api.model.validation.ModelValidationVisitor;
import com.comcast.redirector.api.model.validation.ValidationState;
import com.comcast.redirector.api.model.testsuite.visitor.VisitorFactories;

@ModelValidationVisitor(forClass = ServerGroup.class)
public class ServerGroupExpressionValidationVisitor extends BaseExpressionValidationVisitor<ServerGroup> {
    @Override
    public void visit(ServerGroup item) {

        for (Server server : item.getServers()) {
            if (server instanceof IVisitable) {
                IVisitable visitable = (IVisitable)server;
                ExpressionVisitor<IVisitable> visitor = VisitorFactories.VALIDATION.getFactory().get(visitable.getClass(), getValidationState());
                visitable.accept(visitor);
            }
        }

        try{
            if (Integer.parseInt(item.getCountDownTime()) < -1) {
                getValidationState().pushError(ValidationState.ErrorType.ServerGroupCountDown);
            }
        } catch (NumberFormatException e) {
            getValidationState().pushError(ValidationState.ErrorType.ServerGroupCountDown);
        }


        if (item.getEnablePrivate() == null) {
            getValidationState().pushError(ValidationState.ErrorType.ServerGroupEnablePrivateMissed);
        }
        else if (!item.getEnablePrivate().equalsIgnoreCase("true") && !item.getEnablePrivate().equalsIgnoreCase("false")) {
            getValidationState().pushError(ValidationState.ErrorType.ServerGroupEnablePrivateInvalid);
        }
    }
}
