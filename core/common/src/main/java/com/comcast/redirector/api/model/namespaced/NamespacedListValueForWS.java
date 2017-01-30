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

package com.comcast.redirector.api.model.namespaced;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

/**
 * values for front-end (both decrypted and encrypted)
 * IMPORTANT: encrypted values are to be MANUALLY placed in "ret" values if you want them to be writen to data source
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class NamespacedListValueForWS {

    //in case of encoded NS list type it will contain decoded value
    private String value;

    private String encodedValue;//should be set to null if no encryption is present

    public NamespacedListValueForWS() {
    }

    public NamespacedListValueForWS(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getEncodedValue() {
        return encodedValue;
    }

    public void setEncodedValue(String encodedValue) {
        this.encodedValue = encodedValue;
    }

    /**
     * Equals is overriden to be equal IF AT LEAST ONE OF THOSE VALUES ARE EQUAL
     * (the other value should be null in this case)
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NamespacedListValueForWS that = (NamespacedListValueForWS) o;

        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("NamespaceValueForWS{");
        sb.append("value='").append(value).append('\'');
        sb.append(", encodedValue='").append(encodedValue).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
