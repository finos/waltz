package com.khartec.waltz.service.custom_environment;

import com.khartec.waltz.common.exception.InsufficientPrivelegeException;
import com.khartec.waltz.data.custom_environment.CustomEnvironmentDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.custom_environment.CustomEnvironment;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.permission.PermissionGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

import static java.lang.String.format;

@Service
public class CustomEnvironmentService {

    private final CustomEnvironmentDao customEnvironmentDao;
    private final ChangeLogService changeLogService;
    private final PermissionGroupService permissionGroupService;

    @Autowired
    public CustomEnvironmentService(CustomEnvironmentDao customEnvironmentDao,
                                    ChangeLogService changeLogService,
                                    PermissionGroupService permissionGroupService){
        this.customEnvironmentDao = customEnvironmentDao;
        this.changeLogService = changeLogService;
        this.permissionGroupService = permissionGroupService;
    }


    public Set<CustomEnvironment> findAll(){
        return customEnvironmentDao.findAll();
    }


    public Collection<CustomEnvironment> findByOwningEntityRef(EntityReference ref) {
        return customEnvironmentDao.findByOwningEntityRef(ref);
    }


    public Long create(CustomEnvironment env, String username) throws InsufficientPrivelegeException {
        ensureUserHasPermission(env, username, Operation.ADD);

        Long created = customEnvironmentDao.create(env);
        String message = format(
                "Created new custom environment: %s/%s",
                env.groupName(),
                env.name());
        ChangeLog changeLog = mkChangeLog(
                env.owningEntity(),
                username,
                message,
                Operation.ADD);

        changeLogService.write(changeLog);
        return created;
    }


    public Boolean remove(Long envId, String username) throws InsufficientPrivelegeException {
        CustomEnvironment env = customEnvironmentDao.getById(envId);
        ensureUserHasPermission(env, username, Operation.REMOVE);

        boolean removed = customEnvironmentDao.remove(envId);

        if(removed){
            String message = format(
                    "Deleted custom environment: %s/%s and any mappings to assets",
                    env.groupName(),
                    env.name());
            ChangeLog changeLog = mkChangeLog(
                    env.owningEntity(),
                    username,
                    message,
                    Operation.REMOVE);
            changeLogService.write(changeLog);
        }
        return removed;
    }


    public CustomEnvironment getById(Long id) {
        return customEnvironmentDao.getById(id);
    }


    private ChangeLog mkChangeLog(EntityReference parentRef,
                                  String username,
                                  String message,
                                  Operation operation){

        return ImmutableChangeLog.builder()
                .parentReference(parentRef)
                .userId(username)
                .severity(Severity.INFORMATION)
                .message(message)
                .childKind(EntityKind.CUSTOM_ENVIRONMENT)
                .operation(operation)
                .build();
    }



    private void ensureUserHasPermission(CustomEnvironment env, String username, Operation op) throws InsufficientPrivelegeException {
        boolean hasPerm = permissionGroupService.hasPermission(
                env.owningEntity(),
                EntityKind.CUSTOM_ENVIRONMENT,
                username);

        if (!hasPerm) {
            String msg = format(
                    "Cannot %s environment, insufficient permissions",
                    op.name().toLowerCase());
            throw new InsufficientPrivelegeException(msg);
        }
    }

}

