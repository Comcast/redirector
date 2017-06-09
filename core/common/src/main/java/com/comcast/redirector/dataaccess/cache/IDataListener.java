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
 * @author Sergey Lavrenyuk (slavrenyuk@productengine.com)
 */

package com.comcast.redirector.dataaccess.cache;

public interface IDataListener {
    /**
     * @param eventType type of node event: added, removed, deleted
     * @param path      path to modified node
     * @param data      data of modified node
     */
    void onEvent(EventType eventType, String path, byte[] data, int updateVersion);

    default void onEvent(EventType eventType, String path, byte[] data) {
        this.onEvent(eventType, path, data, 0);
    }

    enum EventType {
        INITIALIZED,
        NODE_ADDED,
        NODE_UPDATED,
        NODE_REMOVED
    }
}
