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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package it.context;

import com.comcast.redirector.api.model.IfExpression;
import com.comcast.redirector.api.model.Server;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.serializers.JAXBContextBuilder;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.XMLSerializer;

import java.util.Collections;

public class TestFlavorRule extends TestModelWrapper<IfExpression> {
    String id;
    String flavor;
    String left;
    String right;
    Operations expression;

    public IfExpression ifExpression(String flavor) {
        Server server = new Server();
        server.setUrl(RedirectorConstants.URL_TEMPLATE);
        server.setPath(flavor);

        IfExpression ifExpression = new IfExpression();
        ifExpression.setId(id);
        ifExpression.setItems(Collections.singletonList(expression.getExpression(left, right)));
        ifExpression.setReturn(server);

        return ifExpression;
    }

    @Override
    public IfExpression value() {
        return ifExpression(flavor);
    }

    public String getId() {
        return id;
    }

    public String getFlavor() {
        return flavor;
    }

    public String getLeftOperand() {
        return left;
    }

    public String getRightOperand() {
        return right;
    }

    public String getWrongRightOperand() {
        return "NOT" + right;
    }

    @Override
    Serializer serializer() {
        return new XMLSerializer(new JAXBContextBuilder().createContextForXML());
    }
}
