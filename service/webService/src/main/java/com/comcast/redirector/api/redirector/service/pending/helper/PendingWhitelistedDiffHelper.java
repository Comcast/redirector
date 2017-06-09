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
 * @author Alexander Pletnev (apletnev@productengine.com)
 */

package com.comcast.redirector.api.redirector.service.pending.helper;

import com.comcast.redirector.api.model.ActionType;
import com.comcast.redirector.api.model.Value;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.google.common.collect.Sets;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class PendingWhitelistedDiffHelper {
    public static Map<String, PendingChange> getWhitelistedDiff(Whitelisted pending, Whitelisted current) {
        Map<String, PendingChange> diff = new TreeMap<>();
        Set<String> allPendingPaths = pending.getPaths() != null ? Sets.newLinkedHashSet(pending.getPaths()) : new LinkedHashSet<String>();
        Set<String> allCurrentPaths = current.getPaths() != null ? Sets.newLinkedHashSet(current.getPaths()) : new LinkedHashSet<String>();

        Set<String> added = getAddedPaths(allPendingPaths, allCurrentPaths);
        Set<String> deleted = getDeletedPaths(allPendingPaths, allCurrentPaths);

        putAddedPathsToDiff(diff, added);
        putDeletedPathsToDiff(diff, deleted);

        return diff;
    }

    private static Set<String> getAddedPaths(Set<String> pending, Set<String> current) {
        Set<String> added = new LinkedHashSet<>();
        Sets.difference(pending, current).copyInto(added);
        return added;
    }

    private static Set<String> getDeletedPaths(Set<String> pending, Set<String> current) {
        Set<String> deleted = new LinkedHashSet<>();
        Sets.difference(current, pending).copyInto(deleted);
        return deleted;
    }

    private static void putAddedPathsToDiff(Map<String, PendingChange> diff, Set<String> paths) {
        for (String path : paths) {
            putPathToDiff(diff, path, path, null, ActionType.ADD);
        }
    }

    private static void putDeletedPathsToDiff(Map<String, PendingChange> diff, Set<String> paths) {
        for (String path : paths) {
            putPathToDiff(diff, path, null, path, ActionType.DELETE);
        }
    }

    private static void putPathToDiff(Map<String, PendingChange> diff, String id, String changed, String current, ActionType action) {
        diff.put(id, new PendingChange(id, action, new Value(changed), new Value(current)));
    }
}
