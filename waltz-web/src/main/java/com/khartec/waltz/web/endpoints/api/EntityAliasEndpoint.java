package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.data.application.AppAliasDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;

@Service
public class EntityAliasEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(EntityAliasEndpoint.class);
    private static final String BASE_URL = mkPath("api", "entity", "alias");


    private final AppAliasDao appAliasDao;
    private final ChangeLogService changeLogService;


    @Autowired
    public EntityAliasEndpoint(AppAliasDao appAliasDao, ChangeLogService changeLogService) {
        checkNotNull(appAliasDao, "appAliasDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.appAliasDao = appAliasDao;
        this.changeLogService = changeLogService;
    }


    @Override
    public void register() {
        String updatePath = mkPath(BASE_URL, ":kind", ":id");
        String getPath = mkPath(BASE_URL, ":kind", ":id");

        ListRoute<String> getRoute = (req, resp) -> {
            EntityReference ref = getEntityReference(req);
            if (ref.kind() != EntityKind.APPLICATION) {
                // TODO: need generic entity_tag table (#236)
                throw new UnsupportedOperationException();
            }
            return appAliasDao.findAliasesForApplication(ref.id());
        };

        ListRoute<String> updateRoute = (req, resp) -> {
            String user = getUsername(req);
            // TODO: ensure user has role...

            EntityReference ref = getEntityReference(req);

            if (ref.kind() != EntityKind.APPLICATION) {
                // TODO: need generic entity_tag table (#236)
                throw new UnsupportedOperationException();
            }

            List<String> tags = readStringsFromBody(req);
            String auditMessage = String.format(
                    "Updated tags, entity: %s, new tags: %s, user: %s",
                    ref,
                    tags,
                    user);

            appAliasDao.updateAliases(ref.id(), tags);

            LOG.info(auditMessage);
            changeLogService.write(ImmutableChangeLog.builder()
                    .parentReference(ref)
                    .userId(user)
                    .message(auditMessage)
                    .severity(Severity.INFORMATION)
                    .build());

            return tags;
        };

        postForList(updatePath, updateRoute);
        getForList(getPath, getRoute);
    }
}
