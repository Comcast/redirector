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

package com.comcast.redirector.metrics;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;

public class Metrics {
    private static final Logger log = LoggerFactory.getLogger(Metrics.class);

    private static MetricsDelegate delegate;

    static {
        ClassLoader cl = Metrics.class.getClassLoader();
        try {
            ClassPath cp = ClassPath.from(cl);
            ImmutableSet<ClassPath.ClassInfo> classes = cp.getTopLevelClassesRecursive(Metrics.class.getPackage().getName());

            for (ClassPath.ClassInfo classInfo : classes) {
                Class metricsDelegateClass = classInfo.load();
                if (! Modifier.isAbstract(metricsDelegateClass.getModifiers()) && MetricsDelegate.class.isAssignableFrom(metricsDelegateClass)) {
                    delegate = (MetricsDelegate) metricsDelegateClass.newInstance();
                    break;
                }
            }

        } catch (Exception e) {
            log.error("Failed to get classes", e);
        }
    }

    public static void reportError500(Throwable throwable) {
        if (delegate != null)
            delegate.reportError500(throwable);
    }

    public static void reportZookeeperConnectionIssue(Throwable throwable) {
        if (delegate != null)
            delegate.reportZookeeperConnectionIssue(throwable);
    }

    public static void reportRestConnectionIssue(Throwable throwable) {
        if (delegate != null)
            delegate.reportRestConnectionIssue(throwable);
    }

    public static void reportConnectionState(String state) {
        if (delegate != null)
            delegate.reportZookeeperConnectionState(state);
    }

    public static void reportGatewayRequestStats() {
        if (delegate != null)
            delegate.reportGatewayRequestStats();
    }

    public static void reportGatewayFailedResponseNoHostsStats() {
        if (delegate != null)
            delegate.reportGatewayFailedResponseNoHostsStats();
    }

    public static void reportGatewayFailedResponseStats() {
        if (delegate != null)
            delegate.reportGatewayFailedResponseStats();
    }

    public static void reportGatewayRedirectDurationStats(long duration) {
        if (delegate != null)
            delegate.reportGatewayRedirectDurationStats(duration);
    }

    public static void reportGatewayTrafficStats(String app, String stack, String version, String tag) {
        if (delegate != null)
            delegate.reportGatewayTrafficStats(app, stack, version, tag);
    }

    public static void reportGatewayModelRefreshStats(String app, int modelVersion, boolean success) {
        if (delegate != null)
            delegate.reportGatewayModelRefreshStats(app, modelVersion, success);
    }

    public static void reportWSApiCallDurationStats(String userAgent, String uri, String method, long duration) {
        if (delegate != null)
            delegate.reportWSApiCallDurationStats(uri, method, userAgent, duration);
    }

    public static void reportWSFailedResponseStats(String userAgent, String uri, String method) {
        if (delegate != null)
            delegate.reportWSFailedResponseStats(uri, method, userAgent);
    }

    public static void reportWSModelApproveStats(String app, int modelVersion) {
        if (delegate != null)
            delegate.reportWSModelApproveStats(app, modelVersion);
    }

    interface MetricsDelegate {
        void reportZookeeperConnectionIssue(Throwable throwable);
        void reportRestConnectionIssue(Throwable throwable);
        void reportZookeeperConnectionState(String state);

        void reportGatewayRequestStats();
        void reportGatewayFailedResponseNoHostsStats();
        void reportGatewayFailedResponseStats();
        void reportGatewayRedirectDurationStats(long duration);

        void reportGatewayTrafficStats(String app, String stack, String version, String tag);
        void reportGatewayModelRefreshStats(String app, int modelVersion, boolean success);

        void reportWSApiCallDurationStats(String uri, String method, String userAgent, long duration);
        void reportWSFailedResponseStats(String uri, String method, String userAgent);

        void reportWSModelApproveStats(String app, int modelVersion);

        void reportError500(Throwable throwable);
    }
}
