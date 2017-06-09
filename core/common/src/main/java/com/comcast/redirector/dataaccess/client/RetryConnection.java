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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

class RetryConnection {
    private static final Logger log = LoggerFactory.getLogger(RetryConnection.class);

    private static final int TIMEOUT = 1000;

    RetryConnection() {

    }

    public synchronized void delay(long timeout, TimeUnit unit) {
        long msecs = unit.toMillis(timeout);
        long endTime = System.currentTimeMillis() + msecs;
        try {
            while (endTime > System.currentTimeMillis()) {
                wait(TIMEOUT);
            }

        } catch (InterruptedException e) {
            log.error("Error wait time ", e.getMessage());
        }

    }
}
