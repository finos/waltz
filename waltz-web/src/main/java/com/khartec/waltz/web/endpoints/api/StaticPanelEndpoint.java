package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.staticpanel.StaticPanel;
import com.khartec.waltz.service.static_panel.StaticPanelService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;


@Service
public class StaticPanelEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "static-panel");

    private final StaticPanelService service;

    @Autowired
    public StaticPanelEndpoint(StaticPanelService service) {
        this.service = service;
    }

    @Override
    public void register() {
        String findByGroupPath = mkPath(BASE_URL, "group", ":group");

        ListRoute<StaticPanel> findByGroupRoute = (request, response) ->
                service.findByGroup(request.params("group"));

        getForList(findByGroupPath, findByGroupRoute);
    }
}
