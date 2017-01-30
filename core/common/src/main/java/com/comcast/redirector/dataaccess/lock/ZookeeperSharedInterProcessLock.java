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

package com.comcast.redirector.dataaccess.lock;

import com.comcast.redirector.dataaccess.client.RedirectorLockReleaseException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.RevocationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ConcurrentModificationException;
import java.util.concurrent.TimeUnit;

public class ZookeeperSharedInterProcessLock implements SharedInterProcessLock {
    private static final Logger log = LoggerFactory.getLogger(ZookeeperSharedInterProcessLock.class);

    private InterProcessMutex mutex;
    private CuratorFramework client;
    private String path;

    private static final int waitTimeoutInSeconds= 150;

    public ZookeeperSharedInterProcessLock(CuratorFramework client, String path) {
        this.client = client;
        this.path = path;
        mutex = new InterProcessMutex(client, path);
        mutex.makeRevocable(new RevocationListener<InterProcessMutex>() {
            @Override
            public void revocationRequested(InterProcessMutex forLock) {
                try {
                    forLock.release();
                } catch (Exception e) {
                    log.error("Error while trying to revoke lock: ", e);
                }
            }
        });
    }

    @Override
    public boolean acquire() {
        try {
            if (mutex.acquire(waitTimeoutInSeconds, TimeUnit.SECONDS)) {
                log.debug("Acquire success");
                return true;
            }
            log.error("Timeout for acquiring lock is expired");
        } catch (Exception e) {
            log.error("Error while trying to acquire lock: ", e);
        }
        return false;
    }

    @Override
    public void release() {
        try {
            mutex.release();
            log.debug("Lock released successfully");
        } catch (Exception e) {
            log.error("Error while releasing lock");
            throw new RedirectorLockReleaseException(e);
        }
    }
}
