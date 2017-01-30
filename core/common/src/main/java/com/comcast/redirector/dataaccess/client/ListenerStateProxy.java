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
package com.comcast.redirector.dataaccess.client;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.listen.Listenable;
import org.apache.curator.framework.state.ConnectionStateListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

class ListenerStateProxy implements Listenable {

    private List<ConnectionStateListener> listeners = new CopyOnWriteArrayList<ConnectionStateListener>();
    private CuratorFramework curator;

    ListenerStateProxy(CuratorFramework curator) {
        this.curator = curator;
    }

    @Override
    public void addListener(Object listener) {
        ConnectionStateListener stateListener = (ConnectionStateListener) listener;
        if (curator != null) {
            curator.getConnectionStateListenable().addListener(stateListener);
        }
        listeners.add(stateListener);
    }

    @Override
    public void addListener(Object listener, Executor executor) {
        ConnectionStateListener stateListener = (ConnectionStateListener) listener;
        if (curator != null) {
            curator.getConnectionStateListenable().addListener(stateListener, executor);
        }
        listeners.add(stateListener);
    }

    @Override
    public void removeListener(Object listener) {
        ConnectionStateListener stateListener = (ConnectionStateListener) listener;
        if (curator != null) {
            curator.getConnectionStateListenable().removeListener(stateListener);
        }
        listeners.remove(stateListener);
    }

    public void clearCurator() {
        for (ConnectionStateListener stateListener : listeners) {
            if (curator != null) {
                curator.getConnectionStateListenable().removeListener(stateListener);
            }
        }
    }

    public void updateCurator(CuratorFramework curator) {
        this.curator = curator;
        for (ConnectionStateListener stateListener : listeners) {
            if (this.curator != null) {
                this.curator.getConnectionStateListenable().addListener(stateListener);
            }
        }
    }

    public List<ConnectionStateListener> getListeners() {
        return listeners;
    }
}
