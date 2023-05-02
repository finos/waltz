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

package org.finos.waltz.service.report_grid;

import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.data.report_grid.ReportGridMemberDao;
import org.finos.waltz.model.person.Person;
import org.finos.waltz.model.report_grid.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class ReportGridMemberService {

    private final ReportGridMemberDao reportGridMemberDao;


    @Autowired
    public ReportGridMemberService(ReportGridMemberDao reportGridMemberDao) {
        checkNotNull(reportGridMemberDao, "reportGridMemberDao cannot be null");

        this.reportGridMemberDao = reportGridMemberDao;
    }


    public Set<ReportGridMember> findByGridId(Long gridId){
        return reportGridMemberDao.findByGridId(gridId);
    }


    public Set<Person> findPeopleByGridId(Long gridId) {
        return reportGridMemberDao.findPeopleByGridId(gridId);
    }


    public int register(long gridId, String username, ReportGridMemberRole role) {
        return reportGridMemberDao.register(gridId, username, role);
    }


    public void checkIsOwner(long gridId, String userId) throws InsufficientPrivelegeException {
        if (!reportGridMemberDao.canUpdate(gridId, userId)) {
            throw new InsufficientPrivelegeException(userId + " cannot update grid: " + gridId);
        }
    }


    public boolean canUpdate(long groupId, String userId){
        return reportGridMemberDao.canUpdate(groupId, userId);
    }


    public Set<ReportGridMember> updateUserRole(long gridId,
                                                ReportGridMemberUpdateRoleCommand updateCommand,
                                                String username) throws InsufficientPrivelegeException {
        checkIsOwner(gridId, username);
        int updatedRoles = reportGridMemberDao.updateUserRole(gridId, updateCommand);
        return findByGridId(gridId);
    }


    public boolean delete(ReportGridMemberDeleteCommand cmd, String username) throws InsufficientPrivelegeException {
        checkIsOwner(cmd.gridId(), username);
        return reportGridMemberDao.delete(cmd);
    }


    public int create(ReportGridMemberCreateCommand cmd, String username) throws InsufficientPrivelegeException {
        checkIsOwner(cmd.gridId(), username);
        return reportGridMemberDao.register(cmd.gridId(), cmd.userId(), cmd.role());
    }

    public int update(ReportGridMemberCreateCommand cmd, String username) throws InsufficientPrivelegeException {
        checkIsOwner(cmd.gridId(), username);
        return reportGridMemberDao.update(cmd, username);
    }
}
