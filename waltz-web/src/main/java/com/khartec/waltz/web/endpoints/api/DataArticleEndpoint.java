package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.data_article.DataArticle;
import com.khartec.waltz.service.data_article.DataArticleService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

@Service
public class DataArticleEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "data-article");

    private final DataArticleService dataArticleService;


    @Autowired
    public DataArticleEndpoint(DataArticleService dataArticleService) {
        checkNotNull(dataArticleService, "dataArticleService cannot be null");
        this.dataArticleService = dataArticleService;
    }


    @Override
    public void register() {
        String findForAppPath = mkPath(BASE_URL, "application", ":id");

        ListRoute<DataArticle> findForAppRoute =
                (request, response) -> dataArticleService.findForAppId(getId(request));

        getForList(findForAppPath, findForAppRoute);
    }
}
