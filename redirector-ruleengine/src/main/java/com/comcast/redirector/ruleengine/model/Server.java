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

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.*;

public class Server extends LanguageElement{
	private String name ="";
	private String url = "";
	private String secureUrl = "";
    private String path = "";
    private Map<String, String> query = new LinkedHashMap<>();
	private String description = "";
    private boolean nonWhitelistOnly;
    private ReturnStatementType returnStatementType;

	public Server() {
	
	}
		
	public Server(String name, String url, String description) {
		super();
		this.name = name;
		this.url = url;
		this.description = description;
	}

	public Server(String name, String url, String description, String secureUrl) {
		this(name, url, description);
		this.secureUrl = secureUrl;
	}

	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getURL() {
		return url;
	}
	public void setURL(String url) {
		this.url = url;
	}

	public Map<String, String> getQuery() {
		return query;
	}

	public void setQuery(Map<String, String> query) {
		this.query = query;
	}

	public String getSecureURL() {
		return secureUrl;
	}
	public void setSecureURL(String url) {
		this.secureUrl = url;
	}

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

    public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append(" url: ");
		sb.append(url);
        sb.append(" query: ");
        sb.append(query);
        sb.append(", Path: [ ");
        sb.append(path);
        sb.append(" ]");

		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Server server = (Server) o;
		return Objects.equals(nonWhitelistOnly, server.nonWhitelistOnly) &&
				Objects.equals(name, server.name) &&
				Objects.equals(url, server.url) &&
				Objects.equals(secureUrl, server.secureUrl) &&
				Objects.equals(path, server.path) &&
				Objects.equals(query, server.query) &&
				Objects.equals(description, server.description) &&
				Objects.equals(returnStatementType, server.returnStatementType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, url, secureUrl, path, query, description, nonWhitelistOnly, returnStatementType);
	}

	@Override
	protected void init(Element element) {
		List<Element> children = getChildElements(element);
		for (Element e : children) {
			String tagName = e.getTagName();
			String value = e.getTextContent().trim();

			if (tagName.equals("name")) {
				name = value;
			} else if (tagName.equals("url")) {
				url = value;
			} else if (tagName.equals("secureUrl")) {
				secureUrl = value;
            } else if (tagName.equals("path")) {
                path = value;
            } else if (tagName.equals("query")) {
                List<Element> pairs = getChildElements(e);
                for (Element pair : pairs) {
                    String key = null;
                    String val = null;
                    NodeList keyNode = pair.getElementsByTagName("key");
                    if (keyNode != null && keyNode.getLength() > 0) {
                        key = keyNode.item(0).getTextContent();
                    }

                    NodeList valueNode = pair.getElementsByTagName("value");
                    if (valueNode != null && valueNode.getLength() > 0) {
                        val = valueNode.item(0).getTextContent();
                    }

                    if (key != null) {
                        query.put(key, val);
                    }
                }
            } else if (tagName.equals("description")) {
                description = value;
            }
		}
        String isWhitelisted = element.getAttribute("isNonWhitelisted");
        if (StringUtils.isNotBlank(isWhitelisted)) {
            nonWhitelistOnly = Boolean.valueOf(isWhitelisted);
        }
		if (url == null || url.isEmpty()) {
            throw new IllegalStateException("Server url is mandatory");
        }
	}

	@Override
	public String toString(int indent, Map<String, String> params) {
		return doSpacing(
            "Server: " + name + " [" + url + "]\n" +
                " [" + query + "]\n [" + secureUrl + "]\n [" + path + "]",
            indent);
	}

    public ReturnStatementType getReturnStatementType() {
        return returnStatementType;
    }

    public void setReturnStatementType(ReturnStatementType returnStatementType) {
        this.returnStatementType = returnStatementType;
    }

    public boolean isNonWhitelisted() {
        return nonWhitelistOnly;
    }

    public void setNonWhitelistOnly(boolean nonWhitelistOnly) {
        this.nonWhitelistOnly = nonWhitelistOnly;
    }
}
