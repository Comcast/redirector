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
 * @author Yuriy Dmitriev (ydmitriev@productengine.com)
 */

package com.comcast.apps.e2e;

import com.comcast.apps.e2e.utils.FileUtil;
import com.comcast.apps.e2e.utils.DataSourceUtil;
import com.comcast.redirector.dataaccess.EntityType;
import com.comcast.redirector.dataaccess.PathHelper;
import com.comcast.redirector.dataaccess.cache.IDataListener;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ZookeeperModelReloadDispatcher {
    private static final Logger log = LoggerFactory.getLogger(ZookeeperModelReloadDispatcher.class);

    private final static String EXPECTED_MODEL_VERSION = "1";
    private final static String FAILED_MODEL_VERSION = "-1";
    private final AtomicInteger stepCount = new AtomicInteger(1);
    private final Lock lock = new ReentrantLock();
    private final Condition runCondition = lock.newCondition();
    private boolean isRunning = true;
    private final String appName;

    public ZookeeperModelReloadDispatcher(String appName) {
        this.appName = appName;
        init();
    }

    private void init() {
        log.info("Zookeeper reload dispatcher init started for '{}'", appName);

        IDataSourceConnector connector = DataSourceUtil.getConnector();
        String modelReloadPath = PathHelper.getPathHelper(EntityType.MODEL_CHANGED, connector.getBasePath()).getPathByService(appName);
        connector.addNodeDataChangeListener(modelReloadPath, new ModelChangeListener());

        log.info("Zookeeper reload dispatcher init ended for '{}'", appName);
    }

    private class ModelChangeListener implements IDataListener {

        @Override
        public void onEvent(EventType eventType, String path, byte[] data, int updateVersion) {
            if (data != null) {
                String currentVersion = new String(data, FileUtil.UTF8_CHARSET);
                log.info("nodeChanged Event: {}", currentVersion);
                //We have to ignore first notification. When we set up and approved whitelist. We can't refresh model while we don't set up all data (default server, distributions, rules, url Rules)
                if (stepCount.get() > 1 && FAILED_MODEL_VERSION.equals(currentVersion)) {
                    log.error("Not updated on init model sync for '{}'", appName);
                    System.exit(1);
                } else {
                    stepCount.getAndIncrement();
                    if (EXPECTED_MODEL_VERSION.equals(currentVersion)) {
                        log.info("Model updated successfully, and current model version = '{}' for '{}'", currentVersion, appName);
                    }
                    runNextStep();
                }
            }
        }
    }

    public void waitNextStep() throws InterruptedException {
        lock.lock();
        try {
            while (isRunning) {
                runCondition.await();
            }
        } finally {
            isRunning = true;
            lock.unlock();
        }
    }

    public void runNextStep() {
        lock.lock();
        try {
            runCondition.signal();
        } finally {
            isRunning = false;
            lock.unlock();
        }
    }
}
