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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.IOException;

/**
 * @goal stop
 * @requiresProject false
 */
public class StopMojo extends AbstractMojo {
    /**
     * @component
     */
    private ZookeeperServer zookeeperServer;

    /**
     * Skip execution
     *
     * @parameter expression="${zoo.skip}" optional="true" default-value="false"
     */
    private Boolean skip;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            return;
        }
        try {
            zookeeperServer.stop();
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to stop the Zookeeper Server", e);
        }
    }
}
