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

import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.authoritative_source.AuthoritativeSourceDao;
import com.khartec.waltz.data.data_flow.DataFlowDao;
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.authoritativesource.AuthoritativeSource;
import com.khartec.waltz.model.authoritativesource.Rating;
import com.khartec.waltz.model.dataflow.DataFlow;
import com.khartec.waltz.model.dataflow.ImmutableRatedDataFlow;
import com.khartec.waltz.model.dataflow.RatedDataFlow;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.model.utils.IdUtilities;
import com.khartec.waltz.service.authoritative_source.AuthoritativeSourceCalculator;
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


@Service
public class RatedDataFlowService {

    private final AuthoritativeSourceCalculator authoritativeSourceCalculator;
    private final OrganisationalUnitDao organisationalUnitDao;
    private final ApplicationDao applicationDao;
    private final DataFlowDao dataFlowDao;

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
                                OrganisationalUnitDao organisationalUnitDao,
                                ApplicationDao applicationDao,
                                DataFlowDao dataFlowDao) {
        checkNotNull(applicationDao, "applicationDao must not be null");
        checkNotNull(organisationalUnitDao, "organisationalUnitDao must not be null");
        checkNotNull(authoritativeSourceCalculator, "authoritativeSourceCalculator must not be null");
        checkNotNull(dataFlowDao, "dataFlowDao must not be null");

        this.authoritativeSourceCalculator = authoritativeSourceCalculator;
        this.organisationalUnitDao = organisationalUnitDao;
        this.applicationDao = applicationDao;
        this.dataFlowDao = dataFlowDao;
    }


    public List<RatedDataFlow> calculateRatedFlowsForOrgUnitTree(long orgUnitId) {

        Map<Long, Map<String, Map<Long, AuthoritativeSource>>> authSourcesByOrgThenTypeThenApp =
                authoritativeSourceCalculator.calculateAuthSourcesForOrgUnitTree(orgUnitId);

        // inclusive
        List<OrganisationalUnit> subUnits = organisationalUnitDao.findDescendants(orgUnitId);
        List<Long> orgUnitIds = IdUtilities.toIds(subUnits);

        List<Application> apps = applicationDao.findByOrganisationalUnitIds(orgUnitIds);
        List<DataFlow> relevantFlows = loadRelevantDataFlows(dataFlowDao, orgUnitIds, apps);
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

    private static Map<String, Collection<AuthoritativeSource>> groupRelevantAuthSourcesByType(
            Map<Long, Collection<AuthoritativeSource>> authSourcesByOrgUnitId,
            Long orgUnitId) {
        Collection<AuthoritativeSource> authSourcesForOrgUnit = authSourcesByOrgUnitId.get(orgUnitId);
        return groupBy(as -> as.dataType(), authSourcesForOrgUnit);
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
                                                        List<Long> orgUnitIds,
                                                        List<Application> apps) {

        List<DataFlow> dataFlows = dataFlowDao.findByOrganisationalUnitIds(orgUnitIds);

        List<Long> appIds = toIds(apps);
        return filter(
                df -> appIds.contains(df.target().id()),
                dataFlows);
    }

    /** (dao, [ ouId ]) -> { ouId -> [authSource] } */
    private static Map<Long, Collection<AuthoritativeSource>> loadAuthSourcesByOrgUnitIds(
            AuthoritativeSourceDao authoritativeSourceDao,
            List<Long> orgUnitIds) {

        List<AuthoritativeSource> allAuthSources = authoritativeSourceDao
                .findByEntityReferences(EntityKind.ORG_UNIT, orgUnitIds);

        return groupBy(
                as -> as.parentReference().id(),
                allAuthSources);
    }


    /** (dao, ouId) -> [ ouId ] */
    private static List<Long> loadOrganisationalUnitIds(OrganisationalUnitDao organisationalUnitDao,
                                                        long orgUnitId) {
        List<OrganisationalUnit> descendants = organisationalUnitDao
                .findDescendants(orgUnitId);

        List<OrganisationalUnit> ancestors = organisationalUnitDao.findAncestors(orgUnitId);

        return toIds(descendants);
    }
}
