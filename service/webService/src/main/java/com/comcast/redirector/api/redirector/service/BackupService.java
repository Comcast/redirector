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
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.api.redirector.service;

import com.comcast.redirector.api.model.BackupUsageSchedule;
import com.comcast.redirector.api.model.ErrorMessage;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.dataaccess.dao.IEmptyObjectDAO;
import com.comcast.redirector.dataaccess.dao.ISimpleServiceDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@Service
public class BackupService implements IBackupService {
    private static Logger log = LoggerFactory.getLogger(BackupService.class);

    @Autowired
    private ISimpleServiceDAO<BackupUsageSchedule> backupUsageScheduleDAO;

    @Autowired
    private IEmptyObjectDAO triggerBackupDAO;

    @Override
    public BackupUsageSchedule getBackupUsageSchedule(String serviceName)  {
        return backupUsageScheduleDAO.get(serviceName);
    }

    @Override
    public void updateBackupUsageSchedule(String serviceName, BackupUsageSchedule schedule)  {
        try {
            backupUsageScheduleDAO.save(schedule, serviceName);
        } catch (SerializerException e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage(e.getMessage())).build());
        }
    }

    @Override
    public void triggerStacksBackup(String serviceName)  {
        triggerBackupDAO.save(serviceName, RedirectorConstants.BACKUP_TRIGGER_PATH);
        log.info("Backup is triggered");
    }
}
