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
package com.comcast.redirector.api.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "defaultModelConstructionDetails")
@XmlAccessorType(XmlAccessType.FIELD)
public class DefaultModelConstructionDetails {

    @XmlAttribute
    private String defaultRouteComposition;

    @XmlAttribute
    private String defaultUrlPartsComposition;

    @XmlAttribute
    private String firstAvailableWhitelisted;

    @XmlAttribute
    private String urlForRedirection;

    public DefaultModelConstructionDetails() {
    }

    public String getDefaultRouteComposition() {
        return defaultRouteComposition;
    }

    public void setDefaultRouteComposition(String defaultServerComposition) {
        this.defaultRouteComposition = defaultServerComposition;
    }

    public String getDefaultUrlPartsComposition() {
        return defaultUrlPartsComposition;
    }

    public void setDefaultUrlPartsComposition(String defaultUrnParamsComposition) {
        this.defaultUrlPartsComposition = defaultUrnParamsComposition;
    }

    public String getFirstAvailableWhitelisted() {
        return firstAvailableWhitelisted;
    }

    public void setFirstAvailableWhitelisted(String firstAvailableWhitelisted) {
        this.firstAvailableWhitelisted = firstAvailableWhitelisted;
    }

    public String getUrlForRedirection() {
        return urlForRedirection;
    }

    public void setUrlForRedirection(String urlForRedirection) {
        this.urlForRedirection = urlForRedirection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultModelConstructionDetails that = (DefaultModelConstructionDetails) o;

        if (defaultRouteComposition != null ? !defaultRouteComposition.equals(that.defaultRouteComposition) : that.defaultRouteComposition != null)
            return false;
        if (defaultUrlPartsComposition != null ? !defaultUrlPartsComposition.equals(that.defaultUrlPartsComposition) : that.defaultUrlPartsComposition != null)
            return false;
        return firstAvailableWhitelisted != null ? firstAvailableWhitelisted.equals(that.firstAvailableWhitelisted) : that.firstAvailableWhitelisted == null && (urlForRedirection != null ? urlForRedirection.equals(that.urlForRedirection) : that.urlForRedirection == null);

    }

    @Override
    public int hashCode() {
        int result = defaultRouteComposition != null ? defaultRouteComposition.hashCode() : 0;
        result = 31 * result + (defaultUrlPartsComposition != null ? defaultUrlPartsComposition.hashCode() : 0);
        result = 31 * result + (firstAvailableWhitelisted != null ? firstAvailableWhitelisted.hashCode() : 0);
        result = 31 * result + (urlForRedirection != null ? urlForRedirection.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DefaultModelConstructionDetails{" +
                "defaultRouteComposition='" + defaultRouteComposition + '\'' +
                ", defaultUrlPartsComposition='" + defaultUrlPartsComposition + '\'' +
                ", firstAvailableWhitelisted='" + firstAvailableWhitelisted + '\'' +
                ", urlForRedirection='" + urlForRedirection + '\'' +
                '}';
    }
}
