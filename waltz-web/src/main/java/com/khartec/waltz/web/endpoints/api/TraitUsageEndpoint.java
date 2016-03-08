package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.trait.Trait;
import com.khartec.waltz.model.trait.TraitUsage;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.trait.TraitService;
import com.khartec.waltz.service.trait.TraitUsageService;
import com.khartec.waltz.service.user.UserService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.deleteForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class TraitUsageEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "trait-usage");
    private static final Logger LOG = getLogger(TraitUsageEndpoint.class);

    private final TraitUsageService traitUsageService;
    private final TraitService traitService;
    private final ChangeLogService changeLogService;
    private final UserService userService;


    @Autowired
    public TraitUsageEndpoint(TraitUsageService traitUsageService,
                              TraitService traitService,
                              ChangeLogService changeLogService,
                              UserService userService) {
        this.traitUsageService = traitUsageService;
        this.traitService = traitService;
        this.changeLogService = changeLogService;
        this.userService = userService;
    }


    @Override
    public void register() {
        String findAllPath = BASE_URL;
        String findByEntityKindPath = mkPath(BASE_URL, "entity", ":kind");
        String findByEntityReferencePath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String findByTraitIdPath = mkPath(BASE_URL, "trait", ":id");
        String addTraitUsagePath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String removeTraitUsagePath = mkPath(BASE_URL, "entity", ":kind", ":id", ":traitId");


        ListRoute<TraitUsage> findAllRoute = (request, response) -> traitUsageService.findAll();
        ListRoute<TraitUsage> findByEntityKindRoute = (request, response) -> traitUsageService.findByEntityKind(getKind(request));
        ListRoute<TraitUsage> findByEntityReferenceRoute = (request, response) -> traitUsageService.findByEntityReference(getEntityReference(request));
        ListRoute<TraitUsage> findByTraitIdRoute = (request, response) -> traitUsageService.findByTraitId(getId(request));

        ListRoute<TraitUsage> addTraitUsageRoute = (request, response) -> {
            requireRole(userService, request, Role.APP_EDITOR);
            long traitId = readBody(request, Long.class);
            EntityReference entityReference = getEntityReference(request);

            Trait trait = traitService.getById(traitId);

            if (trait == null) {
                LOG.warn("Could not find trait to add");
                return traitUsageService.findByEntityReference(entityReference);
            }

            List<TraitUsage> usages = traitUsageService.addTraitUsage(entityReference, traitId);

            changeLogService.write(ImmutableChangeLog.builder()
                    .severity(Severity.INFORMATION)
                    .parentReference(entityReference)
                    .userId(getUser(request).userName())
                    .message("Added trait: " + trait.name())
                    .build());

            return usages;
        };

        ListRoute<TraitUsage> removeTraitUsageRoute = (request, response) -> {
            requireRole(userService, request, Role.APP_EDITOR);
            long traitId = getLong(request, "traitId");
            EntityReference entityReference = getEntityReference(request);

            Trait trait = traitService.getById(traitId);

            if (trait == null) {
                LOG.warn("Could not find trait to remove, proceeding anyway");
                // we may be cleaning up incorrect historical data
            }

            List<TraitUsage> usages = traitUsageService.removeTraitUsage(entityReference, traitId);

            changeLogService.write(ImmutableChangeLog.builder()
                    .severity(Severity.INFORMATION)
                    .parentReference(entityReference)
                    .userId(getUser(request).userName())
                    .message("Removed trait: "+ (trait == null ? traitId : trait.name()))
                    .build());

            return usages;
        };


        getForList(findAllPath, findAllRoute);
        getForList(findByEntityKindPath, findByEntityKindRoute);
        getForList(findByEntityReferencePath, findByEntityReferenceRoute);
        getForList(findByTraitIdPath, findByTraitIdRoute);

        postForList(addTraitUsagePath, addTraitUsageRoute);
        deleteForList(removeTraitUsagePath, removeTraitUsageRoute);

    }
}
