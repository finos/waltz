package com.khartec.waltz.service.permission;

import com.khartec.waltz.service.involvement.InvolvementService;
import com.khartec.waltz.service.person.PersonService;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.involvement.Involvement;
import org.finos.waltz.model.permission_group.Permission;
import org.finos.waltz.model.person.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Service
public class PermissionGroupService {
    private static final Logger LOG = LoggerFactory.getLogger(PermissionGroupService.class);

    private final InvolvementService involvementService;
    private final PersonService personService;
    private final PermissionGroupDao permissionGroupDao;

    @Autowired
    public PermissionGroupService(InvolvementService involvementService,
                                  PersonService personService,
                                  PermissionGroupDao permissionGroupDao) {
        this.involvementService = involvementService;
        this.personService = personService;
        this.permissionGroupDao = permissionGroupDao;
    }

    public List<Permission> findPermissions(EntityReference parentEntityRef,
                                            String username) {
        Person person = personService.getPersonByUserId(username);

        if (isNull(person)){
            return Collections.emptyList();
        }

        List<Involvement> involvements =
                involvementService.findByEmployeeId(person.employeeId())
                        .stream()
                        .filter(involvement -> involvement.entityReference().equals(parentEntityRef))
                        .collect(Collectors.toList());

        if (involvements.isEmpty()) {
            return Collections.emptyList();
        }

        return permissionGroupDao.getDefaultPermissions();
    }

    public boolean hasPermission(EntityReference entityReference,
                                 EntityKind qualifierKind,
                                 String username) {
        return findPermissions(entityReference, username)
                .stream()
                .anyMatch(permission -> permission.qualifierKind().equals(qualifierKind));
    }
}
