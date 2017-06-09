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
 * @author Stanislav Menshykov (smenshykov@productengine.com)
 */

package com.comcast.apps.e2e.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class TaskChain {
    private List<Function<Context, Boolean>> tasks;

    public TaskChain(Function<Context, Boolean> initialTask) {
        this.tasks = new ArrayList<>();
        tasks.add(initialTask);
    }

    public TaskChain and(Function<Context, Boolean> task) {
        tasks.add(task);
        return this;
    }

    public Boolean execute(Context context) {
        Boolean result = false;
        for (Function<Context, Boolean> task : tasks) {
            result = task.apply(context);
            if (!result) {
                break;
            }
        }

        return result;
    }
}
