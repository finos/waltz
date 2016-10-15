package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.ProduceConsumeGroup;
import com.khartec.waltz.model.physical_data_article.PhysicalDataArticle;
import com.khartec.waltz.service.physical_data_article.PhysicalDataArticleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.WebUtilities.readIdSelectionOptionsFromBody;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;

@Service
public class PhysicalDataArticleEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "physical-data-article");

    private final PhysicalDataArticleService dataArticleService;


    @Autowired
    public PhysicalDataArticleEndpoint(PhysicalDataArticleService dataArticleService) {
        checkNotNull(dataArticleService, "dataArticleService cannot be null");
        this.dataArticleService = dataArticleService;
    }


    @Override
    public void register() {
        String findByProducerAppPath = mkPath(
                BASE_URL,
                "application",
                ":id",
                "produces");

        String findByConsumerAppIdPath = mkPath(
                BASE_URL,
                "application",
                ":id",
                "consumes");

        String findByAppPath = mkPath(
                BASE_URL,
                "application",
                ":id");

        String findBySelectorPath = mkPath(
                BASE_URL,
                "selector");

        String getByIdPath = mkPath(
                BASE_URL,
                "id",
                ":id");

        ListRoute<PhysicalDataArticle> findByProducerAppRoute =
                (request, response) -> dataArticleService.findByProducerAppId(getId(request));

        ListRoute<PhysicalDataArticle> findByConsumerAppIdRoute =
                (request, response) -> dataArticleService.findByConsumerAppId(getId(request));

        ListRoute<PhysicalDataArticle> findBySelectorRoute =
                (request, response) -> dataArticleService.findBySelector(readIdSelectionOptionsFromBody(request));

        DatumRoute<ProduceConsumeGroup<PhysicalDataArticle>> findByAppRoute =
                (request, response) -> dataArticleService.findByAppId(getId(request));

        DatumRoute<PhysicalDataArticle> getByIdRoute =
                (request, response) -> dataArticleService.getById(getId(request));

        getForList(findByProducerAppPath, findByProducerAppRoute);
        getForList(findByConsumerAppIdPath, findByConsumerAppIdRoute);
        postForList(findBySelectorPath, findBySelectorRoute);

        getForDatum(findByAppPath, findByAppRoute);
        getForDatum(getByIdPath, getByIdRoute);
    }
}
