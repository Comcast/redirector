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
 * @author Stanislav Menshykov (smenshykov@productengine.com)
 */
package com.comcast.redirector.common.executor;

import com.comcast.redirector.common.util.ThreadLocalLogger;

import java.util.concurrent.*;

public class LoggingExecutor extends ThreadPoolExecutor {

    private LoggingExecutor(int corePoolSize,
                            int maximumPoolSize,
                            long keepAliveTime,
                            TimeUnit unit,
                            BlockingQueue<Runnable> workQueue,
                            ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public static class Factory {
        public static LoggingExecutor newSingleThreadExecutor(ThreadFactory threadFactory) {
            return new LoggingExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(),
                    threadFactory);
        }
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        ThreadLocalLogger.clear();
        super.beforeExecute(t, r);
    }
}
