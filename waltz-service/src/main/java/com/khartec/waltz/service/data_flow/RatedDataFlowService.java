/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service.data_flow;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.data_flow.DataFlowDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.authoritativesource.AuthoritativeSource;
import com.khartec.waltz.model.authoritativesource.Rating;
import com.khartec.waltz.model.dataflow.DataFlow;
import com.khartec.waltz.model.dataflow.ImmutableRatedDataFlow;
import com.khartec.waltz.model.dataflow.RatedDataFlow;
import com.khartec.waltz.service.authoritative_source.AuthoritativeSourceCalculator;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.filter;
import static com.khartec.waltz.common.MapUtilities.*;
import static com.khartec.waltz.model.utils.IdUtilities.toIds;


@Deprecated
@Service
public class RatedDataFlowService {

    private final AuthoritativeSourceCalculator authoritativeSourceCalculator;
    private final ApplicationDao applicationDao;
    private final DataFlowDao dataFlowDao;
    private final ApplicationIdSelectorFactory applicationIdSelectorFactory;

    /** (dataflow, (type -> [authSource])) -> Rating */
    private static final BiFunction<DataFlow, Map<String, Map<Long, AuthoritativeSource>>, Rating> lookupRatingFn =
            (df, authSourceByTypeThenApp) -> {
                Map<Long, AuthoritativeSource> authSourcesByApp = authSourceByTypeThenApp.get(df.dataType());

                if (isEmpty(authSourcesByApp)) {
                    return Rating.NO_OPINION;
                }

                long sourceAppId = df.source().id();

                AuthoritativeSource authoritativeSource = authSourcesByApp.get(sourceAppId);

                return authoritativeSource == null
                        ? Rating.DISCOURAGED
                        : authoritativeSource.rating();
            };


    @Autowired
    public RatedDataFlowService(AuthoritativeSourceCalculator authoritativeSourceCalculator,
                                ApplicationDao applicationDao,
                                DataFlowDao dataFlowDao, ApplicationIdSelectorFactory applicationIdSelectorFactory) {
        checkNotNull(applicationDao, "applicationDao must not be null");
        checkNotNull(authoritativeSourceCalculator, "authoritativeSourceCalculator must not be null");
        checkNotNull(dataFlowDao, "dataFlowDao must not be null");
        Checks.checkNotNull(applicationIdSelectorFactory, "applicationIdSelectorFactory cannot be null");

        this.authoritativeSourceCalculator = authoritativeSourceCalculator;
        this.applicationDao = applicationDao;
        this.dataFlowDao = dataFlowDao;
        this.applicationIdSelectorFactory = applicationIdSelectorFactory;
    }


    public List<RatedDataFlow> calculateRatedFlowsForOrgUnitTree(long orgUnitId) {

        Map<Long, Map<String, Map<Long, AuthoritativeSource>>> authSourcesByOrgThenTypeThenApp =
                authoritativeSourceCalculator.calculateAuthSourcesForOrgUnitTree(orgUnitId);

        Select<Record1<Long>> appIdSelector = createAppIdSelectorBasedOnOrgUnitId(orgUnitId);

        List<Application> apps = applicationDao.findByAppIdSelector(appIdSelector);

        List<DataFlow> relevantFlows = loadRelevantDataFlows(
                dataFlowDao,
                apps);

        Map<Long, Collection<DataFlow>> flowsByOrgUnitId = groupFlowsByTargetOrgUnitId(apps, relevantFlows);

        return flowsByOrgUnitId.entrySet()
                .stream()
                .map(entry -> {
                    long owningOrgUnitId = entry.getKey();

                    Map<String, Map<Long, AuthoritativeSource>> authSourcesByTypeThenApp =
                            authSourcesByOrgThenTypeThenApp.get(owningOrgUnitId);
                    Collection<DataFlow> flows = entry.getValue();
                    return prepareResult(flows, authSourcesByTypeThenApp);
                })
                .flatMap(l -> l.stream())
                .collect(Collectors.toList());
    }

    private Select<Record1<Long>> createAppIdSelectorBasedOnOrgUnitId(long orgUnitId) {
        IdSelectionOptions selectionOptions = ImmutableIdSelectionOptions.builder()
                .entityReference(ImmutableEntityReference.builder()
                        .kind(EntityKind.ORG_UNIT)
                        .id(orgUnitId)
                        .build())
                .scope(HierarchyQueryScope.CHILDREN)
                .build();

        return applicationIdSelectorFactory.apply(selectionOptions);
    }


    private static List<RatedDataFlow> prepareResult(
            Collection<DataFlow> flows,
            Map<String, Map<Long, AuthoritativeSource>> authSourceByTypeThenApp) {
        return flows
                .stream()
                .map(f -> {
                    Rating rating = lookupRatingFn.apply(f, authSourceByTypeThenApp);
                    return ImmutableRatedDataFlow.builder()
                            .dataFlow(f)
                            .rating(rating)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private static Map<Long, Collection<DataFlow>> groupFlowsByTargetOrgUnitId(List<Application> apps,
                                                                               List<DataFlow> relevantFlows) {
        Function<DataFlow, Long> lookupOrgUnitIdFn = mkLookupTargetOrgUnitIdFn(apps);

        return groupBy(
                lookupOrgUnitIdFn,
                relevantFlows);
    }


    /**
     * [app] -> dataFlow -> ouId
     *
     * @return function which takes a data flow and returns the ouId of the target application
     */
    private static Function<DataFlow, Long> mkLookupTargetOrgUnitIdFn(List<Application> apps) {
        Map<Long, Application> appsById = indexBy(a -> a.id().get(), apps);

        return df -> {
            long appId = df.target().id();
            Application app = appsById.get(appId);
            if (app == null) {
                return null;
            } else {
                return app.organisationalUnitId();
            }
        };
    }


    /**
     * (dao, [ouId], [app]) -> [ dataFlow ]
     * <br>
     * @return a list of data flows where the target is a member of [app].
     */
    private static List<DataFlow> loadRelevantDataFlows(DataFlowDao dataFlowDao,
                                                        List<Application> targetApps) {

        List<Long> appIds = toIds(targetApps);

        List<DataFlow> dataFlows = dataFlowDao.findByApplicationIds(appIds);

        return filter(
                df -> appIds.contains(df.target().id()),
                dataFlows);
    }



}
