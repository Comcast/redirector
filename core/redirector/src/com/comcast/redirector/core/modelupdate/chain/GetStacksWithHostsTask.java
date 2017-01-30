package com.comcast.redirector.core.modelupdate.chain;

import com.comcast.redirector.api.model.xrestack.StackData;
import com.comcast.redirector.common.util.ThreadLocalLogger;

import java.util.Set;
import java.util.function.Supplier;

public class GetStacksWithHostsTask implements IProcessTask {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(GetStacksWithHostsTask.class);
    private Supplier<Set<StackData>> stacksSupplier;
    
    public GetStacksWithHostsTask(Supplier<Set<StackData>> stacksSupplier) {
        this.stacksSupplier = stacksSupplier;
    }
    
    @Override
    public Result handle(ModelContext context) {
        Set<StackData> stackDateSet = stacksSupplier.get();

        if (stackDateSet.size() > 0) {
            context.setStackData(stackDateSet);
            log.info("Hosts are successfully loaded for model of version={}, found {} host(s)", context.getModelVersion(), stackDateSet.size());
            return Result.success(context);
        } else {
            log.warn("Failed to load hosts for model of version={}", context.getModelVersion());
            return Result.failure(context);
        }
    }
}
