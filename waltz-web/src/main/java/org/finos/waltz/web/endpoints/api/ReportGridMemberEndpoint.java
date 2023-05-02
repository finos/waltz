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

package org.finos.waltz.web.endpoints.api;

import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.model.person.Person;
import org.finos.waltz.model.report_grid.ReportGridMember;
import org.finos.waltz.model.report_grid.ReportGridMemberCreateCommand;
import org.finos.waltz.model.report_grid.ReportGridMemberDeleteCommand;
import org.finos.waltz.model.report_grid.ReportGridMemberUpdateRoleCommand;
import org.finos.waltz.service.report_grid.ReportGridMemberService;
import org.finos.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Set;

import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class ReportGridMemberEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "report-grid-member");

    private final ReportGridMemberService reportGridMemberService;


    @Autowired
    public ReportGridMemberEndpoint(ReportGridMemberService reportGridMemberService) {

        this.reportGridMemberService = reportGridMemberService;
    }


    @Override
    public void register() {
        String findForGridIdPath = mkPath(BASE_URL, "grid-id", ":id");
        String findPeopleForGridIdPath = mkPath(BASE_URL, "grid-id", ":id", "people");
        String updateUserRolePath = mkPath(BASE_URL, "grid-id", ":id", "update-role");
        String createPath = mkPath(BASE_URL, "create");
        String updatePath = mkPath(BASE_URL, "update");
        String deletePath = mkPath(BASE_URL, "delete");

        getForList(findForGridIdPath, this::findForGridIdRoute);
        getForList(findPeopleForGridIdPath, this::findPeopleForGridIdRoute);
        postForList(updateUserRolePath, this::updateUserRoleRoute);
        postForDatum(createPath, this::createRoute);
        postForDatum(updatePath, this::updateRoute);
        postForDatum(deletePath, this::deleteRoute);
    }


    public Set<ReportGridMember> findForGridIdRoute(Request req, Response resp) throws IOException {
        return reportGridMemberService.findByGridId(getId(req));
    }


    public Set<Person> findPeopleForGridIdRoute(Request req, Response resp) throws IOException {
        return reportGridMemberService.findPeopleByGridId(getId(req));
    }


    public Set<ReportGridMember> updateUserRoleRoute(Request req, Response resp) throws IOException, InsufficientPrivelegeException {
        return reportGridMemberService.updateUserRole(
                getId(req),
                readBody(req, ReportGridMemberUpdateRoleCommand.class),
                getUsername(req));
    }


    @Deprecated
    public int createRoute(Request req, Response resp) throws IOException, InsufficientPrivelegeException {
        return reportGridMemberService.create(
                readBody(req, ReportGridMemberCreateCommand.class),
                getUsername(req));
    }

    public int updateRoute(Request req, Response resp) throws IOException, InsufficientPrivelegeException {
        return reportGridMemberService.update(
                readBody(req, ReportGridMemberCreateCommand.class),
                getUsername(req));
    }


    public boolean deleteRoute(Request req, Response resp) throws IOException, InsufficientPrivelegeException {
        ReportGridMemberDeleteCommand cmd = readBody(req, ReportGridMemberDeleteCommand.class);
        return reportGridMemberService.delete(
                cmd,
                getUsername(req));
    }
}