/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.service.changelog;

import com.khartec.waltz.common.CollectionUtilities;
import com.khartec.waltz.data.DBExecutorPoolInterface;
import com.khartec.waltz.data.changelog.ChangeLogDao;
import com.khartec.waltz.data.physical_flow.PhysicalFlowDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.physical_flow.PhysicalFlow;
import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

import static com.khartec.waltz.common.Checks.*;
import static com.khartec.waltz.model.EntityReference.mkRef;


@Service
public class ChangeLogService {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeLogService.class);
    private final ChangeLogDao changeLogDao;
    private final DBExecutorPoolInterface dbExecutorPool;
    private final PhysicalFlowDao physicalFlowDao;


    @Autowired
    public ChangeLogService(ChangeLogDao changeLogDao,
                            DBExecutorPoolInterface dbExecutorPool,
                            PhysicalFlowDao physicalFlowDao) {
        checkNotNull(changeLogDao, "changeLogDao must not be null");
        checkNotNull(dbExecutorPool, "dbExecutorPool cannot be null");
        checkNotNull(physicalFlowDao, "physicalFlowDao cannot be null");

        this.changeLogDao = changeLogDao;
        this.dbExecutorPool = dbExecutorPool;
        this.physicalFlowDao = physicalFlowDao;
    }


    public List<ChangeLog> findByParentReference(EntityReference ref,
                                                 Optional<Integer> limit) {
        checkNotNull(ref, "ref must not be null");
        if(ref.kind() == EntityKind.PHYSICAL_FLOW) {
            return findByParentReferenceForPhysicalFlow(ref, limit);
        }
        return changeLogDao.findByParentReference(ref, limit);
    }


    public List<ChangeLog> findByPersonReference(EntityReference ref,
                                                 Optional<Integer> limit) {
        checkNotNull(ref, "ref must not be null");
        return changeLogDao.findByPersonReference(ref, limit);
    }


    public List<ChangeLog> findByUser(String userName,
                                      Optional<Integer> limit) {
        checkNotEmpty(userName, "Username cannot be empty");
        return changeLogDao.findByUser(userName, limit);
    }


    public int write(ChangeLog changeLog) {
        return changeLogDao.write(changeLog);
    }


    public int[] write(List<ChangeLog> changeLogs) {
        return changeLogDao.write(changeLogs);
    }


    ////////////////////// PRIVATE HELPERS //////////////////////////////////////////

    private List<ChangeLog> findByParentReferenceForPhysicalFlow(EntityReference ref,
                                                                 Optional<Integer> limit) {
        checkNotNull(ref, "ref must not be null");
        checkTrue(ref.kind() == EntityKind.PHYSICAL_FLOW, "ref should refer to a Physical Flow");

        Future<List<ChangeLog>> flowLogsFuture = dbExecutorPool.submit(() -> changeLogDao.findByParentReference(ref, limit));

        Future<List<ChangeLog>> specLogsFuture = dbExecutorPool.submit(() -> {
            PhysicalFlow flow = physicalFlowDao.getById(ref.id());
            return changeLogDao.findByParentReference(mkRef(EntityKind.PHYSICAL_SPECIFICATION, flow.specificationId()), limit);
        });

        return Unchecked.supplier(() -> {
            List<ChangeLog> flowLogs = flowLogsFuture.get();
            List<ChangeLog> specLogs = specLogsFuture.get();
            List<ChangeLog> all = new ArrayList<>();
            all.addAll(flowLogs);
            all.addAll(specLogs);
            return (List<ChangeLog>) CollectionUtilities.sort(all, Comparator.comparing(ChangeLog::createdAt).reversed());
        }).get();
    }

}
