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
import com.comcast.redirector.core.modelupdate.chain.validator.ValidationReport;
import com.comcast.redirector.core.modelupdate.chain.validator.Validator;
import com.comcast.redirector.core.modelupdate.data.ModelMetadata;
import com.comcast.redirector.core.modelupdate.holder.IModelHolder;

public class SyncModelsTask implements IProcessTask {
    private static final ThreadLocalLogger log = new ThreadLocalLogger(SyncModelsTask.class);
    private IModelHolder<ModelMetadata> modelMetadataHolder;

    public SyncModelsTask(IModelHolder<ModelMetadata> modelMetadataHolder) {
        this.modelMetadataHolder = modelMetadataHolder;
    }

    @Override
    public Result handle(ModelContext context) {
        ModelMetadata metadataFromBackup = modelMetadataHolder.load(IModelHolder.GET_FROM_BACKUP);

        if (metadataFromBackup == null) {
            log.info("Since version of model is not available in backups. Let's update");
        } else if (context.getModelVersion() > metadataFromBackup.getVersion()) {
            log.info("Zookeeper has new model. version = {} (local version = {}). Let's update",
                    context.getModelVersion(), metadataFromBackup.getVersion());
        } else {
            ValidationReport report = new ValidationReport(Validator.ValidationResultType.SKIP_SYNC_MODELS_AS_THEY_ALREADY_IN_SYNC,
                    "Zookeeper and Local models are in sync: DS=" + context.getModelVersion() + ", Backup=" + metadataFromBackup.getVersion() + ". No need to update model");
            log.info(report.getMessage());
            return Result.failure(context, report);
        }
        return Result.success(context);
    }
}
