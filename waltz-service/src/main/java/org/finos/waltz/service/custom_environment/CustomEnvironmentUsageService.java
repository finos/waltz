package org.finos.waltz.service.custom_environment;

import org.finos.waltz.model.permission_group.CheckPermissionCommand;
import org.finos.waltz.model.permission_group.ImmutableCheckPermissionCommand;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.service.permission.PermissionGroupService;
import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.data.custom_environment.CustomEnvironmentUsageDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.Severity;
import org.finos.waltz.model.changelog.ChangeLog;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.custom_environment.CustomEnvironment;
import org.finos.waltz.model.custom_environment.CustomEnvironmentUsage;
import org.finos.waltz.model.custom_environment.CustomEnvironmentUsageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static java.lang.String.format;
import static org.finos.waltz.model.EntityReferenceUtilities.pretty;

@Service
public class CustomEnvironmentUsageService {

    private final CustomEnvironmentUsageDao customEnvironmentUsageDao;
    private final CustomEnvironmentService customEnvironmentService;
    private final ChangeLogService changeLogService;
    private final PermissionGroupService permissionGroupService;

    @Autowired
    public CustomEnvironmentUsageService(CustomEnvironmentUsageDao customEnvironmentUsageDao,
                                         CustomEnvironmentService customEnvironmentService,
                                         ChangeLogService changeLogService,
                                         PermissionGroupService permissionGroupService){
        this.customEnvironmentUsageDao = customEnvironmentUsageDao;
        this.customEnvironmentService = customEnvironmentService;
        this.changeLogService = changeLogService;
        this.permissionGroupService = permissionGroupService;
    }


    public Set<CustomEnvironmentUsage> findUsagesByOwningEntityRef(EntityReference ref) {
        return customEnvironmentUsageDao.findByOwningEntityRef(ref);
    }


    public Long addAsset(CustomEnvironmentUsage usage, String username) throws InsufficientPrivelegeException {
        CustomEnvironment customEnvironment = customEnvironmentService.getById(usage.customEnvironmentId());

        ensureUserHasPermission(customEnvironment, username, Operation.ADD);

        Long usageId = customEnvironmentUsageDao.addAsset(usage, username);
        CustomEnvironmentUsageInfo usageInfo = customEnvironmentUsageDao.getUsageInfoById(usageId);
        String message = format(
                "Added asset: %s to custom environment: %s/%s",
                pretty(usageInfo.asset().entityReference()),
                customEnvironment.groupName(),
                customEnvironment.name());

        ChangeLog changeLog = mkChangeLog(
                customEnvironment.owningEntity(),
                username,
                message,
                Operation.ADD);

        changeLogService.write(changeLog);

        return usageId;
    }


    public Boolean remove(Long usageId, String username) throws InsufficientPrivelegeException {
        CustomEnvironmentUsageInfo usageInfo = customEnvironmentUsageDao.getUsageInfoById(usageId);
        CustomEnvironment customEnvironment = customEnvironmentService.getById(usageInfo.usage().customEnvironmentId());

        ensureUserHasPermission(customEnvironment, username, Operation.REMOVE);

        boolean remove = customEnvironmentUsageDao.remove(usageId);

        if(remove) {
            String message = format("Removed asset: %s from custom environment: %s/%s",
                    pretty(usageInfo.asset().entityReference()),
                    customEnvironment.groupName(),
                    customEnvironment.name());

            ChangeLog changeLog = mkChangeLog(customEnvironment.owningEntity(), username, message, Operation.REMOVE);
            changeLogService.write(changeLog);
        }
        return remove;
    }


    public Set<CustomEnvironmentUsageInfo> findUsageInfoByOwningEntity(EntityReference ref){
        return customEnvironmentUsageDao.findUsageInfoByOwningRef(ref);
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
                .childKind(EntityKind.CUSTOM_ENVIRONMENT_USAGE)
                .operation(operation)
                .build();
    }


    private void ensureUserHasPermission(CustomEnvironment env,
                                         String username,
                                         Operation op) throws InsufficientPrivelegeException {
        CheckPermissionCommand cmd = ImmutableCheckPermissionCommand
                .builder()
                .operation(op)
                .user(username)
                .parentEntityRef(env.owningEntity())
                .subjectKind(EntityKind.CUSTOM_ENVIRONMENT)
                .build();

        boolean hasPerm = permissionGroupService.hasPermission(cmd);

        if (!hasPerm) {
            String msg = format(
                    "Cannot %s environment usage, insufficient permissions",
                    op.name().toLowerCase());
            throw new InsufficientPrivelegeException(msg);
        }
    }

}

