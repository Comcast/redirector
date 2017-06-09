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

package com.comcast.redirector.core.modelupdate.chain;

import com.comcast.redirector.common.util.ThreadLocalLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class TaskChain {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(TaskChain.class);
    private List<Function<ModelContext, Result>> tasks;

    public TaskChain(Function<ModelContext, Result> initialTask) {
        this.tasks = new ArrayList<>();
        tasks.add(initialTask);
    }

    public TaskChain and(Function<ModelContext, Result> task) {
        tasks.add(task);
        return this;
    }

    public Result execute(ModelContext modelContext) {
        Result result = Result.failure(modelContext);
        for (Function<ModelContext, Result> task : tasks) {
            result = task.apply(modelContext);
            if (!result.isSuccessful()) {
                log.error("failedTask={} ", task.getClass().getSimpleName());
                break;
            }

            modelContext = result.getContext();
        }

        return result;
    }

}
