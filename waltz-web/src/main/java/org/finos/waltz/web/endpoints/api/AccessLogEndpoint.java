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

import org.finos.waltz.model.Duration;
import org.finos.waltz.model.accesslog.AccessLogSummary;
import org.finos.waltz.service.access_log.AccessLogService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.WaltzVersionInfo;
import org.finos.waltz.model.accesslog.AccessLog;
import org.finos.waltz.model.accesslog.AccessTime;
import org.finos.waltz.model.accesslog.ImmutableAccessLog;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.time.LocalDate;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.DateTimeUtilities.today;
import static org.finos.waltz.common.EnumUtilities.readEnum;


@Service
public class AccessLogEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("api", "access-log");

    private final AccessLogService accessLogService;
    private final WaltzVersionInfo waltzVersionInfo;
    private final UserRoleService userRoleService;


    @Autowired
    public AccessLogEndpoint(AccessLogService accessLogService,
                             WaltzVersionInfo waltzVersionInfo,
                             UserRoleService userRoleService) {
        checkNotNull(accessLogService, "accessLogService cannot be null");
        checkNotNull(waltzVersionInfo, "waltzVersionInfo cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.accessLogService = accessLogService;
        this.waltzVersionInfo = waltzVersionInfo;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {

        // -- paths ---------------------------------------------------------
        String findForUserPath = WebUtilities.mkPath(BASE_URL, "user", ":userId");
        String findActiveUsersPath = WebUtilities.mkPath(BASE_URL, "active", ":minutes");
        String writePath = WebUtilities.mkPath(BASE_URL, ":state");
        String findAccessCountsByStatePath = WebUtilities.mkPath(BASE_URL, "counts_by_state", ":days");
        String findAccessLogCountsPath = WebUtilities.mkPath(BASE_URL, "counts_since", ":days");
        String findDailyActiveUserCountsSincePath = WebUtilities.mkPath(BASE_URL, "users_since", ":days");
        String findYearOnYearUsersPath = WebUtilities.mkPath(BASE_URL, "summary", "year_on_year", ":mode");
        String findAccessLogYearsPath = WebUtilities.mkPath(BASE_URL, "get_years");
        String findMonthOnMonthUsersPath = WebUtilities.mkPath(BASE_URL, "summary", "month_on_month", ":mode", ":year");
        String findUsersSummaryPath = WebUtilities.mkPath(BASE_URL, "summary", "period", ":frequency");
        String findTopPagesPath = WebUtilities.mkPath(BASE_URL, "analytics", "top-pages");
        String findActivityByHourPath = WebUtilities.mkPath(BASE_URL, "analytics", "activity-by-hour");
        String findActivityByDayPath = WebUtilities.mkPath(BASE_URL, "analytics", "activity-by-day");
        String findTopUsersPath = WebUtilities.mkPath(BASE_URL, "analytics", "top-users");
        String findSessionDurationsPath = WebUtilities.mkPath(BASE_URL, "analytics", "session-durations");

        // -- routes --------------------------------------------------------
        ListRoute<AccessLog> findForUserRoute = (request, response) ->
                accessLogService.findForUserId(request.params("userId"), WebUtilities.getLimit(request));

        ListRoute<AccessTime> findActiveUsersRoute = (request, response) -> {
            java.time.Duration minutes = java.time.Duration.ofMinutes(Integer.parseInt(request.params("minutes")));
            return accessLogService.findActiveUsersSince(minutes);
        };

        ListRoute<AccessLogSummary> findAccessLogCountsByStateSince = (request, response) -> {
            java.time.Duration days = java.time.Duration.ofDays(Integer.parseInt(request.params("days")));
            return accessLogService.findAccessLogCountsByStateSince(days);
        };

        ListRoute<AccessLogSummary> findWeeklyAccessLogSummary = (request, response) -> {
            java.time.Duration days = java.time.Duration.ofDays(Integer.parseInt(request.params("days")));
            return accessLogService.findWeeklyAccessLogSummary(days);
        };

        ListRoute<AccessLogSummary> findDailyActiveUserCountsSince = (request, response) -> {
            java.time.Duration days = java.time.Duration.ofDays(Integer.parseInt(request.params("days")));
            return accessLogService.findDailyUniqueUsersSince(days);
        };

        ListRoute<AccessLogSummary> findYearOnYearUsersRoute = (request, response) ->
                accessLogService.findYearOnYearAccessLogSummary(request.params("mode"));

        ListRoute<Integer> findAccessLogYearsRoute = (request, response) -> accessLogService.findAccessLogYears();

        ListRoute<AccessLogSummary> findMonthOnMonthUsersRoute = (request, response) -> {
            String mode = request.params("mode");
            Integer year = Integer.parseInt(request.params("year"));
            return accessLogService.findMonthOnMonthAccessLogSummary(mode, year);
        };

        ListRoute<AccessLogSummary> findUsersSummaryRoute = (request, response) -> {
            WebUtilities.requireRole(userRoleService, request, SystemRole.ADMIN);
            Duration frequency = readFrequency(request.params("frequency"));
            LocalDate endDate = readEndDate(request);
            LocalDate startDate = readStartDate(request, endDate);
            return accessLogService.findAccessLogSummary(frequency, startDate, endDate);
        };

        ListRoute<AccessLogSummary> findTopPagesRoute = (request, response) -> {
            WebUtilities.requireRole(userRoleService, request, SystemRole.ADMIN);
            LocalDate endDate = readEndDate(request);
            LocalDate startDate = readStartDate(request, endDate);
            return accessLogService.findTopPagesByAccess(startDate, endDate, readLimit(request));
        };

        ListRoute<AccessLogSummary> findActivityByHourRoute = (request, response) -> {
            WebUtilities.requireRole(userRoleService, request, SystemRole.ADMIN);
            LocalDate endDate = readEndDate(request);
            LocalDate startDate = readStartDate(request, endDate);
            return accessLogService.findActivityByHourOfDay(startDate, endDate);
        };

        ListRoute<AccessLogSummary> findActivityByDayRoute = (request, response) -> {
            WebUtilities.requireRole(userRoleService, request, SystemRole.ADMIN);
            LocalDate endDate = readEndDate(request);
            LocalDate startDate = readStartDate(request, endDate);
            return accessLogService.findActivityByDayOfWeek(startDate, endDate);
        };

        ListRoute<AccessLogSummary> findTopUsersRoute = (request, response) -> {
            WebUtilities.requireRole(userRoleService, request, SystemRole.ADMIN);
            LocalDate endDate = readEndDate(request);
            LocalDate startDate = readStartDate(request, endDate);
            return accessLogService.findTopActiveUsers(startDate, endDate, readLimit(request));
        };

        ListRoute<AccessLogSummary> findSessionDurationsRoute = (request, response) -> {
            WebUtilities.requireRole(userRoleService, request, SystemRole.ADMIN);
            LocalDate endDate = readEndDate(request);
            LocalDate startDate = readStartDate(request, endDate);
            return accessLogService.findSessionDurations(startDate, endDate);
        };

        // -- registrations -------------------------------------------------
        EndpointUtilities.getForList(findForUserPath, findForUserRoute);
        EndpointUtilities.getForList(findActiveUsersPath, findActiveUsersRoute);
        EndpointUtilities.postForDatum(writePath, this::writeRoute);
        EndpointUtilities.getForList(findAccessCountsByStatePath, findAccessLogCountsByStateSince);
        EndpointUtilities.getForList(findAccessLogCountsPath, findWeeklyAccessLogSummary);
        EndpointUtilities.getForList(findDailyActiveUserCountsSincePath, findDailyActiveUserCountsSince);
        EndpointUtilities.getForList(findYearOnYearUsersPath, findYearOnYearUsersRoute);
        EndpointUtilities.getForList(findAccessLogYearsPath, findAccessLogYearsRoute);
        EndpointUtilities.getForList(findMonthOnMonthUsersPath, findMonthOnMonthUsersRoute);
        EndpointUtilities.getForList(findUsersSummaryPath, findUsersSummaryRoute);
        EndpointUtilities.getForList(findTopPagesPath, findTopPagesRoute);
        EndpointUtilities.getForList(findActivityByHourPath, findActivityByHourRoute);
        EndpointUtilities.getForList(findActivityByDayPath, findActivityByDayRoute);
        EndpointUtilities.getForList(findTopUsersPath, findTopUsersRoute);
        EndpointUtilities.getForList(findSessionDurationsPath, findSessionDurationsRoute);
    }


    private static final int DEFAULT_LIMIT = 10;
    private static final int DEFAULT_RANGE_MONTHS = 6;


    private static Duration readFrequency(String frequency) {
        String normalised = frequency == null ? null : frequency.toUpperCase();
        return readEnum(normalised, Duration.class, s -> Duration.MONTH);
    }

    private static LocalDate readEndDate(Request request) {
        return WebUtilities.getLocalDateQueryParam(request, "endDate").orElseGet(() -> today());
    }

    private static LocalDate readStartDate(Request request, LocalDate endDate) {
        return WebUtilities.getLocalDateQueryParam(request, "startDate")
                .orElseGet(() -> endDate.minusMonths(DEFAULT_RANGE_MONTHS));
    }

    private static int readLimit(Request request) {
        String limitParam = request.queryParams("limit");
        return limitParam != null ? Integer.parseInt(limitParam) : DEFAULT_LIMIT;
    }


    private WaltzVersionInfo writeRoute(Request request, Response response) {
        AccessLog accessLog = ImmutableAccessLog.builder()
                .userId(WebUtilities.getUsername(request))
                .state(request.params("state"))
                .params(request.body())
                .build();

        accessLogService.write(accessLog);

        return waltzVersionInfo;
    }

}
