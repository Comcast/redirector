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
 */
package com.comcast.redirector.ruleengine.model;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServerGroup extends LanguageElement {
	private List<Server> servers = new ArrayList<>();
	private boolean enablePrivateServers = true;
	private int countDownTime = -1;

	@Override
	protected void init(Element element) {
		List<Element> children = getChildElements(element);
		
		// get the public servers
		if (children != null && children.size() > 0) {
			for (Element e : children) {
				if (! e.getTagName().trim().equals("server")) {
					throw new IllegalArgumentException("Children of <servergroup> must be <server>'s or empty; found " + e.getTagName());
				}
				Server server = (Server) model.createLanguageElement(e);
				servers.add(server);
			}
			
		}
		
		// are private servers disabled?
		String enablePrivate = element.getAttribute("enablePrivate");
		if (enablePrivate != null && enablePrivate.toLowerCase().trim().equals("false")) {
			enablePrivateServers = false;
		}
		
		String time = element.getAttribute("countDownTime");
		if (time != null) {
			try {
				setCountDownTime(Integer.parseInt(time));
			} catch(Exception e) {
				throw new IllegalArgumentException("Error parsing countDownTime", e);
			}
		}
		
	}

	@Override
	public String toString(int indent, Map<String, String> params) {
		StringBuilder sb = new StringBuilder();
		sb.append(doSpacing("serverGroup [", indent));
		if (servers != null && servers.size() > 0) {
			for (Server server : servers) {
				sb.append(server.toString(indent += printSpacing, params));
			}
 		}
		sb.append(doSpacing("]", indent));
		return null;
	}
	
	public List<Server> getServers() {
		return servers;
	}

	public void setEnablePrivateServers(boolean enablePrivateServers) {
		this.enablePrivateServers = enablePrivateServers;
	}

	public boolean isEnablePrivateServers() {
		return enablePrivateServers;
	}

	public void setCountDownTime(int countDownTime) {
		this.countDownTime = countDownTime;
	}

	public int getCountDownTime() {
		return countDownTime;
	}

}
