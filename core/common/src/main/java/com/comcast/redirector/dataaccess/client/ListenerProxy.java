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
 */
package com.comcast.redirector.dataaccess.client;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.listen.Listenable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

class ListenerProxy implements Listenable {

    private List<CuratorListener> listeners = new CopyOnWriteArrayList<CuratorListener>();
    private CuratorFramework curator;

    ListenerProxy(CuratorFramework curator) {
        this.curator = curator;
    }

    @Override
    public void addListener(Object listener) {
        CuratorListener curatorListener = (CuratorListener) listener;
        if (curator != null) {
            curator.getCuratorListenable().addListener(curatorListener);
        }
        listeners.add(curatorListener);
    }

    @Override
    public void addListener(Object listener, Executor executor) {
        CuratorListener curatorListener = (CuratorListener) listener;
        if (curator != null) {
            curator.getCuratorListenable().addListener(curatorListener, executor);
        }
        listeners.add(curatorListener);
    }

    @Override
    public void removeListener(Object listener) {
        CuratorListener curatorListener = (CuratorListener) listener;
        if (curator != null) {
            curator.getCuratorListenable().removeListener(curatorListener);
        }
        listeners.remove(curatorListener);
    }

    public void clearCurator() {
        for (CuratorListener curatorListener : listeners) {
            if (curator != null) {
                curator.getCuratorListenable().removeListener(curatorListener);
            }
        }
    }

    public void updateCurator(CuratorFramework curator) {
        this.curator = curator;
        for (CuratorListener curatorListener : listeners) {
            if (this.curator != null) {
                this.curator.getCuratorListenable().addListener(curatorListener);
            }
        }
    }
}
