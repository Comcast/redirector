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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */
package com.comcast.apps.redirector.maven.zookeeper;

import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingServer;

import java.io.IOException;

public class ZookeeperServer {
    private TestingServer zookeeperServer;

    public void start(int port, int tickTime, int maxConnections) throws Exception {
        InstanceSpec spec = new InstanceSpec(null, port, -1, -1, true, -1, tickTime, maxConnections);
        zookeeperServer = new TestingServer(spec, false);
        zookeeperServer.start();
    }

    public void stop() throws IOException {
        if (zookeeperServer != null) {
            zookeeperServer.close();
        }
    }
}
