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

package com.khartec.waltz.service.end_user_app;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.changelog.ChangeLogDao;
import com.khartec.waltz.data.end_user_app.EndUserAppDao;
import com.khartec.waltz.data.end_user_app.EndUserAppIdSelectorFactory;
import com.khartec.waltz.data.involvement.InvolvementDao;
import com.khartec.waltz.data.orgunit.OrganisationalUnitIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.application.AppRegistrationRequest;
import com.khartec.waltz.model.application.AppRegistrationResponse;
import com.khartec.waltz.model.application.ApplicationKind;
import com.khartec.waltz.model.application.ImmutableAppRegistrationRequest;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.enduserapp.EndUserApplication;
import com.khartec.waltz.model.involvement.ImmutableInvolvement;
import com.khartec.waltz.model.involvement.Involvement;
import com.khartec.waltz.model.rating.RagRating;
import com.khartec.waltz.model.tally.Tally;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.FunctionUtilities.time;
import static com.khartec.waltz.common.ListUtilities.map;
import static com.khartec.waltz.model.EntityKind.APPLICATION;
import static com.khartec.waltz.model.EntityReference.mkRef;

@Service
public class EndUserAppService {

    private final EndUserAppDao endUserAppDao;
    private final ApplicationDao applicationDao;
    private final ChangeLogDao changeLogDao;
    private final InvolvementDao involvementDao;
    private final EndUserAppIdSelectorFactory endUserAppIdSelectorFactory = new EndUserAppIdSelectorFactory();
    private final OrganisationalUnitIdSelectorFactory orgUnitIdSelectorFactory= new OrganisationalUnitIdSelectorFactory();


    @Autowired
    public EndUserAppService(EndUserAppDao endUserAppDao,
                             ApplicationDao applicationDao,
                             ChangeLogDao changeLogDao,
                             InvolvementDao involvementDao) {
        checkNotNull(endUserAppDao, "EndUserAppDao is required");
        checkNotNull(applicationDao, "ApplicationDao is required");
        checkNotNull(changeLogDao, "ChangeLogDao is required");
        checkNotNull(involvementDao, "InvolvementDao is required");
        this.endUserAppDao = endUserAppDao;
        this.applicationDao = applicationDao;
        this.changeLogDao = changeLogDao;
        this.involvementDao = involvementDao;
    }


    @Deprecated
    public List<EndUserApplication> findByOrganisationalUnitSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = orgUnitIdSelectorFactory.apply(options);
        return time("EUAS.findByOrganisationalUnitSelector", () -> endUserAppDao.findByOrganisationalUnitSelector(selector));
    }


    public Collection<Tally<Long>> countByOrgUnitId() {
        return time("EUAS.countByOrgUnitId", () -> endUserAppDao.countByOrganisationalUnit());
    }


    public List<EndUserApplication> findBySelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = endUserAppIdSelectorFactory.apply(options);
        return time("EUAS.findBySelector", () -> endUserAppDao.findBySelector(selector));
    }


    public List<EndUserApplication> findAll() {
        return endUserAppDao.findAll();
    }


    public AppRegistrationResponse promoteToApplication(Long id, String username){

        AppRegistrationRequest appRegistrationRequest = mkAppRegistrationRequestForEuda(id);

        checkNotNull(appRegistrationRequest, "EUDA has already been promoted");

        endUserAppDao.updateIsPromotedFlag(id);

        AppRegistrationResponse appRegistrationResponse = applicationDao.registerApp(appRegistrationRequest);

        migrateEudaInvolvements(id, appRegistrationResponse);

        changeLogDao.write(mkChangeLog(appRegistrationResponse, username));

        return appRegistrationResponse;
    }


    private void migrateEudaInvolvements(Long id, AppRegistrationResponse appRegistrationResponse) {

        List<Involvement> eudaInvolvements = involvementDao.findByEntityReference(mkRef(EntityKind.END_USER_APPLICATION, id));

        List<Involvement> appInvolvements = map(eudaInvolvements,
                r -> ImmutableInvolvement.builder()
                        .entityReference(mkRef(APPLICATION, appRegistrationResponse.id().get()))
                        .kindId(r.kindId())
                        .employeeId(r.employeeId())
                        .provenance(r.provenance())
                        .isReadOnly(false)
                        .build());

        appInvolvements.forEach(involvementDao::save);
    }


    private ChangeLog mkChangeLog(AppRegistrationResponse appRegistrationResponse, String username) {
        return ImmutableChangeLog.builder()
                .message(String.format("Promoted application '%s' from an end user application", appRegistrationResponse.originalRequest().name()))
                .operation(Operation.ADD)
                .parentReference(mkRef(APPLICATION, appRegistrationResponse.id().get()))
                .userId(username)
                .severity(Severity.INFORMATION)
                .childKind(EntityKind.END_USER_APPLICATION)
                .createdAt(DateTimeUtilities.nowUtc())
                .build();
    }


    private AppRegistrationRequest mkAppRegistrationRequestForEuda(Long id) {

        EndUserApplication euda = endUserAppDao.getById(id);

        if(euda.isPromoted()){
            return null;
        } else {
            return ImmutableAppRegistrationRequest
                    .builder()
                    .name(euda.name())
                    .applicationKind(ApplicationKind.EUC)
                    .assetCode(String.valueOf(euda.id().get()))
                    .businessCriticality(euda.riskRating())
                    .description(euda.description() + "<br>["+ euda.applicationKind() + "]")
                    .lifecyclePhase(euda.lifecyclePhase())
                    .organisationalUnitId(euda.organisationalUnitId())
                    .overallRating(RagRating.R)
                    .addAliases("")
                    .addTags("")
                    .provenance(euda.provenance())
                    .build();
        }
    }

}
