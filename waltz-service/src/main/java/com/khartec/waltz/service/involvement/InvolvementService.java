/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service.involvement;

import com.khartec.waltz.data.EntityReferenceNameResolver;
import com.khartec.waltz.data.end_user_app.EndUserAppIdSelectorFactory;
import com.khartec.waltz.data.involvement.InvolvementDao;
import com.khartec.waltz.data.person.PersonDao;
import com.khartec.waltz.model.EntityIdSelectionOptions;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.EntityReferenceUtilities;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.enduserapp.EndUserApplication;
import com.khartec.waltz.model.involvement.EntityInvolvementChangeCommand;
import com.khartec.waltz.model.involvement.Involvement;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.involvement_kind.InvolvementKindService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.FunctionUtilities.time;
import static com.khartec.waltz.common.ListUtilities.applyToFirst;
import static com.khartec.waltz.common.ListUtilities.newArrayList;


@Service
public class InvolvementService {


    private final ChangeLogService changeLogService;
    private final InvolvementDao dao;
    private final EndUserAppIdSelectorFactory endUserAppIdSelectorFactory;
    private final EntityReferenceNameResolver entityReferenceNameResolver;
    private final InvolvementKindService involvementKindService;
    private final PersonDao personDao;

    private Map<Long, String> involvementKindIdToNameMap;


    @Autowired
    public InvolvementService(ChangeLogService changeLogService,
                              InvolvementDao dao,
                              EndUserAppIdSelectorFactory endUserAppIdSelectorFactory,
                              EntityReferenceNameResolver entityReferenceNameResolver,
                              InvolvementKindService involvementKindService,
                              PersonDao personDao) {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(dao, "dao must not be null");
        checkNotNull(endUserAppIdSelectorFactory, "endUserAppIdSelectorFactory cannot be null");
        checkNotNull(entityReferenceNameResolver, "entityReferenceNameResolver cannot be null");
        checkNotNull(involvementKindService, "involvementKindService cannot be null");
        checkNotNull(personDao, "personDao cannot be null");

        this.changeLogService = changeLogService;
        this.dao = dao;
        this.endUserAppIdSelectorFactory = endUserAppIdSelectorFactory;
        this.entityReferenceNameResolver = entityReferenceNameResolver;
        this.involvementKindService = involvementKindService;
        this.personDao = personDao;
    }


    public List<Involvement> findByEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return time("IS.findByEntityReference", () -> dao.findByEntityReference(ref));
    }


    public List<Application> findDirectApplicationsByEmployeeId(String employeeId) {
        checkNotEmpty(employeeId, "employeeId cannot be empty");
        return time("IS.findDirectApplicationsByEmployeeId", () -> dao.findDirectApplicationsByEmployeeId(employeeId));
    }


    public List<Application> findAllApplicationsByEmployeeId(String employeeId) {
        checkNotEmpty(employeeId, "employeeId cannot be empty");
        return time("IS.findAllApplicationsByEmployeeId", () -> dao.findAllApplicationsByEmployeeId(employeeId));
    }


    public List<EndUserApplication> findAllEndUserApplicationsBySelector(EntityIdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        return time("IS.findAllEndUserApplicationsBySelector",
                () -> dao.findAllEndUserApplicationsByEmployeeId(endUserAppIdSelectorFactory.apply(options)));
    }


    public List<Involvement> findByEmployeeId(String employeeId) {
        checkNotEmpty(employeeId, "employeeId cannot be empty");
        return dao.findByEmployeeId(employeeId);
    }


    public List<Person> findPeopleByEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return time("IS.findPeopleByEntityReference", () -> dao.findPeopleByEntityReference(ref));
    }

    public boolean addEntityInvolvement(String userId,
                                        EntityReference entityReference,
                                        EntityInvolvementChangeCommand command) {
        Involvement involvement = mkInvolvement(entityReference, command);
        boolean result = dao.save(involvement) == 1;
        if (result) {
            logChange(entityReference, userId, command);
        }
        return result;
    }


    public boolean removeEntityInvolvement(String userId,
                                           EntityReference entityReference,
                                           EntityInvolvementChangeCommand command) {
        Involvement involvement = mkInvolvement(entityReference, command);
        boolean result = dao.remove(involvement) == 1;
        if (result) {
            logChange(entityReference, userId, command);
        }
        return result;
    }


    private Involvement mkInvolvement(EntityReference entityReference,
                                      EntityInvolvementChangeCommand command) {
        checkNotNull(entityReference, "entityReference cannot be null");
        checkNotNull(command, "command cannot be null");

        Person person = personDao.getById(command.personEntityRef().id());

        return Involvement.mkInvolvement(
                entityReference,
                person.employeeId(),
                command.involvementKindId(),
                "waltz");
    }


    private void logChange(EntityReference entityReference, String userId, EntityInvolvementChangeCommand command) {
        String message = String.format("Involvement kind (%s) %s for person: %s",
                resolvePrettyInvolvementKind(command.involvementKindId()),
                command.operation().name().toLowerCase(),
                resolveName(command.personEntityRef()));

        ImmutableChangeLog changeLog = ImmutableChangeLog.builder()
                .parentReference(entityReference)
                .message(message)
                .userId(userId)
                .childKind(command.personEntityRef().kind())
                .operation(command.operation())
                .build();
        changeLogService.write(changeLog);
    }


    private String resolvePrettyInvolvementKind(long id) {
        if(involvementKindIdToNameMap == null) {
            this.involvementKindIdToNameMap = loadInvolvementKindIdToNameMap();
        }

        return String.format("%s / %s", this.involvementKindIdToNameMap.get(id), id);
    }


    private String resolveName(EntityReference ref) {
        return applyToFirst(entityReferenceNameResolver.resolve(newArrayList(ref)),
                EntityReferenceUtilities::pretty).orElseGet(() -> ref.toString());
    }


    private Map<Long, String> loadInvolvementKindIdToNameMap() {
        return involvementKindService
                .findAll()
                .stream()
                .collect(Collectors.toMap(ik -> ik.id().get(), ik -> ik.name()));
    }

}
