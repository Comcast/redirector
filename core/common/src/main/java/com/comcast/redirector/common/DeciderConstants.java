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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package com.comcast.redirector.common;

public class DeciderConstants {


    public static final String DECIDER_PATH = "/decider/";
    public static final String DECIDER_RULES_PATH = DECIDER_PATH + "deciderRules";
    public static final String DECIDER_PARTNERS_PATH = DECIDER_PATH + "partners";
    public static final String DECIDER_ZOOKEEPER_PATH = "PartnerDecider";
    public static final String RULES_PATH = "rules";
    public static final String FULL_SERVICE_PATH = "/" + DECIDER_ZOOKEEPER_PATH + "/";
}
