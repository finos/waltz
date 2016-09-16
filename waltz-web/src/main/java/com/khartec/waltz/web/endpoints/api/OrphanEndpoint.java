package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.service.orphan.OrphanService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.user.Role.ADMIN;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.WebUtilities.requireRole;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

@Service
public class OrphanEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(OrphanEndpoint.class);
    private static final String BASE_URL = mkPath("api", "orphan");

    private final OrphanService orphanService;
    private final UserRoleService userRoleService;


    @Autowired
    public OrphanEndpoint(OrphanService orphanService, UserRoleService userRoleService) {
        checkNotNull(orphanService, "orphanService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.orphanService = orphanService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String findApplicationsWithNonExistingOrgUnitPath = mkPath(BASE_URL, "application-non-existing-org-unit");


        ListRoute<EntityReference> findApplicationsWithNonExistingOrgUnitRoute = (request, response) -> {
            requireRole(userRoleService, request, ADMIN);
            return orphanService.findApplicationsWithNonExistingOrgUnit();
        };

        getForList(findApplicationsWithNonExistingOrgUnitPath, findApplicationsWithNonExistingOrgUnitRoute);
    }
}
