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

package com.comcast.redirector.dataaccess.dao;

import com.comcast.redirector.api.model.ErrorMessage;
import com.comcast.redirector.api.model.Value;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.namespaced.NamespacedListType;
import com.comcast.redirector.api.model.namespaced.NamespacedListValueForWS;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.dataaccess.IPathHelper;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

public class NamespacedListsDAO extends ListDAO<NamespacedList> {
    private static String ENCODING_TYPE = "MD5";

    public NamespacedListsDAO(Serializer marshalling, IDataSourceConnector connector, IPathHelper pathHelper, boolean isCompressed, boolean useCache) {
        super(NamespacedList.class, marshalling, connector, pathHelper, isCompressed, useCache);
    }

    @Override
    public List<NamespacedList> getAll() {
        List<NamespacedList> namespacedLists = super.getAll();
        for (NamespacedList namespacedList: namespacedLists) {
            convertToFrontendFormat(namespacedList);
        }
        return namespacedLists;
    }

    @Override
    public NamespacedList getById(String id) {
        NamespacedList list = super.getById(id);
        return convertToFrontendFormat(list);
    }

    @Override
    public void saveById(NamespacedList data, String id) throws SerializerException {
        super.saveById(convertToBackendFormat(data), id);
        convertToFrontendFormat(data);
    }

    @Override
    public void deleteById(String id) {
        super.deleteById(id);
    }

    //is public because offline mode needs to perform encryption as well
    public static String hashNamespacedListValue(String raw) {
        try {
            return DatatypeConverter.printHexBinary(MessageDigest.getInstance(ENCODING_TYPE).digest(raw.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new WebApplicationException(Response.serverError().entity(new ErrorMessage("MD5 hashing is not supported")).build());
        }
    }

    public static NamespacedList convertToFrontendFormat (NamespacedList list) {
        if (list != null) {
            list.setValueSet(new LinkedHashSet<>());
            for (Value value : list.getRet()) {
                NamespacedListValueForWS namespacedListValueForWS = new NamespacedListValueForWS();
                namespacedListValueForWS.setValue(value.getValue());
                if (NamespacedListType.ENCODED.equals(list.getType())) {
                    namespacedListValueForWS.setValue(value.getValue());
                    namespacedListValueForWS.setEncodedValue(hashNamespacedListValue(value.getValue()));
                } else {
                    namespacedListValueForWS.setValue(value.getValue());
                }
                list.getValueSet().add(namespacedListValueForWS);
            }
            list.setRet(null);
            if (Objects.isNull(list.getType())) {
                list.setType(NamespacedListType.TEXT);
            }
        }
        return list;
    }

    //is public due to backwards compatibility issues (need to support old format).
    public static NamespacedList convertToBackendFormat (NamespacedList list) {
        list.setRet(new LinkedHashSet<>());
        for (NamespacedListValueForWS valueForFrontend: list.getValueSet()) {
            Value value = new Value();
            value.setValue(valueForFrontend.getValue());
            list.getRet().add(value);
        }
        list.setValueSet(null);
        list.setValueCount(null);
        if (NamespacedListType.ENCODED.equals(list.getType())) {
            list.setValuesEncodingType(ENCODING_TYPE);
        }
        return list;
    }
}
