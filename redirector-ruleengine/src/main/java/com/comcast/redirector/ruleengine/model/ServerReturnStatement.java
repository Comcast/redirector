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

package com.comcast.redirector.ruleengine.model;

import org.w3c.dom.Element;

public class ServerReturnStatement extends ReturnStatement {
    public ServerReturnStatement(Server languageElement) {
        this.value = languageElement;
    }

    @Override
    protected void init(Element element) {
        // look for a servergroup if it exists, initialize it
        // if there is no servergroup, look for a Server
        // if there is no server, get the string value and create a server
        if (element.getTagName().trim().equals(Model.TAG_SERVER_GROUP)) {
            value = (ServerGroup) model.createLanguageElement(element);
        } else if (element.getTagName().trim().equals(Model.TAG_SERVER)) {
            value = (Server) model.createLanguageElement(element);
        }
    }
}
