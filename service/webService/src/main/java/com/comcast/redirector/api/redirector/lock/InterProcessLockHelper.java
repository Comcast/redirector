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
 * @author Alexey Mironchenko (amironchenko@productengine.com)
 */

package com.comcast.redirector.api.redirector.lock;

import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.dataaccess.client.RedirectorDataSourceException;
import com.comcast.redirector.dataaccess.lock.SharedInterProcessLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.comcast.redirector.common.RedirectorConstants.*;

@Component
public class InterProcessLockHelper implements LockHelper {

    @Autowired
    private IDataSourceConnector connector;

    private Map<String, SharedInterProcessLock> lockMap = new HashMap<>();

    @Override
    public SharedInterProcessLock getLock(String application, EntityType entityType) {
        if (! connector.isConnected()) {
            throw new RedirectorDataSourceException("No connection to zookeeper");
        }

        String lockPath = Stream
            .of(REDIRECTOR_ZOOKEEPER_PATH, SERVICES_PATH, application, REDIRECTOR_LOCK_PATH, entityType.getPath())
            .collect(Collectors.joining(DELIMETER, DELIMETER, ""));

        if (lockMap.containsKey(lockPath)) {
            return lockMap.get(lockPath);
        } else {
            SharedInterProcessLock lock = connector.createLock(lockPath);
            lockMap.put(lockPath, lock);
            return lock;
        }
    }
}
