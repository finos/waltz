package com.khartec.waltz.service.custom_environment;

import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import com.khartec.waltz.data.custom_environment.CustomEnvironmentUsageDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.custom_environment.CustomEnvironment;
import com.khartec.waltz.model.custom_environment.CustomEnvironmentUsage;
import com.khartec.waltz.model.custom_environment.CustomEnvironmentUsageInfo;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.permission.PermissionGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.khartec.waltz.model.EntityReferenceUtilities.pretty;
import static java.lang.String.format;

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
        boolean hasPerm = permissionGroupService.hasPermission(
                env.owningEntity(),
                EntityKind.CUSTOM_ENVIRONMENT,
                username);

        if (!hasPerm) {
            String msg = format(
                    "Cannot %s environment usage, insufficient permissions",
                    op.name().toLowerCase());
            throw new InsufficientPrivelegeException(msg);
        }
    }

}

