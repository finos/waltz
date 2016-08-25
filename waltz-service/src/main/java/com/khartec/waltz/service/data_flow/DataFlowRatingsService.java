package com.khartec.waltz.service.data_flow;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.common.MapUtilities;
import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.data.authoritative_source.AuthoritativeSourceDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.authoritativesource.AuthoritativeRatingVantagePoint;
import com.khartec.waltz.model.authoritativesource.Rating;
import com.khartec.waltz.model.dataflow.DataFlow;
import com.khartec.waltz.model.dataflow.ImmutableDataFlow;
import com.khartec.waltz.service.application.ApplicationService;
import com.khartec.waltz.service.authoritative_source.AuthoritativeSourceResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class DataFlowRatingsService {

    private ApplicationService applicationService;
    private AuthoritativeSourceDao authoritativeSourceDao;

    @Autowired
    public DataFlowRatingsService(ApplicationService applicationService,
                                  AuthoritativeSourceDao authoritativeSourceDao) {
        checkNotNull(applicationService, "applicationService cannot be null");
        checkNotNull(authoritativeSourceDao, "authoritativeSourceDao cannot be null");

        this.applicationService = applicationService;
        this.authoritativeSourceDao = authoritativeSourceDao;
    }




    public List<DataFlow> calculateRatings(List<DataFlow> flows) throws SQLException {

        // need to retrieve the rating for each sourceApp, targetApp, datatype combination
        // first turn targetApps into owning orgUnits
        List<Long> targetApplicationIds = ListUtilities.map(
                flows,
                df -> df.target().id());
        List<Application> targetApps = applicationService.findByIds(targetApplicationIds);
        Map<Long, Application> targetAppsById = MapUtilities.indexBy(a -> a.id().get(), targetApps);
        Set<Long> targetOuIds = SetUtilities.map(targetApps, app -> app.organisationalUnitId());

        AuthoritativeSourceResolver resolver = createResolver(targetOuIds);

        return ListUtilities.map(
                flows,
                f -> {
                    EntityReference orgUnit = ImmutableEntityReference.builder()
                            .kind(EntityKind.ORG_UNIT)
                            .id(targetAppsById.get(f.target().id()).organisationalUnitId())
                            .build();
                    Rating rating = resolver.resolve(orgUnit, f.source(), f.dataType());
                    return ImmutableDataFlow.copyOf(f).withRating(rating);
                });
    }



    private AuthoritativeSourceResolver createResolver(Set<Long> orgIds) {
        List<AuthoritativeRatingVantagePoint> authoritativeRatingVantagePoints =
                authoritativeSourceDao.findAuthoritativeRatingVantagePoints(orgIds);

        AuthoritativeSourceResolver resolver = new AuthoritativeSourceResolver(authoritativeRatingVantagePoints);
        return resolver;
    }


}
