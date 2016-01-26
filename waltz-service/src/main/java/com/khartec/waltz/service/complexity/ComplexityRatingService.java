/*
 *  This file is part of Waltz.
 *
 *  Waltz is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Waltz is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.khartec.waltz.service.complexity;

import com.khartec.waltz.model.complexity.ComplexityRating;
import com.khartec.waltz.model.complexity.ComplexityScore;
import com.khartec.waltz.model.complexity.ImmutableComplexityRating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.MapUtilities.indexBy;
import static com.khartec.waltz.common.MapUtilities.maybeGet;
import static com.khartec.waltz.common.SetUtilities.union;
import static java.util.Optional.ofNullable;


@Service
public class ComplexityRatingService {

    private final CapabilityComplexityService capabilityComplexityService;
    private final ServerComplexityService serverComplexityService;
    private final ConnectionComplexityService connectionComplexityService;


    @Autowired
    public ComplexityRatingService(ConnectionComplexityService connectionComplexityService,
                                   CapabilityComplexityService capabilityComplexityService,
                                   ServerComplexityService serverComplexityService) {
        this.connectionComplexityService = connectionComplexityService;
        this.capabilityComplexityService = capabilityComplexityService;
        this.serverComplexityService = serverComplexityService;
    }


    public ComplexityRating getForApp(long appId) {
        ComplexityScore serverComplexity = serverComplexityService.getForApp(appId);
        ComplexityScore connectionComplexity = connectionComplexityService.getForApp(appId);
        ComplexityScore capabilityComplexity = capabilityComplexityService.getForApp(appId);

        return ImmutableComplexityRating.builder()
                .connectionComplexity(ofNullable(connectionComplexity))
                .serverComplexity(ofNullable(serverComplexity))
                .capabilityComplexity(ofNullable(capabilityComplexity))
                .build();
    }


    /**
     * Find connection complexity of applications within a given organisational unit (and it's
     * sub units).  The complexity are baselined against the application with the most
     * connections in the system.  If you wish specify a specific baseline use
     * the overloaded method.
     * @param orgUnitId
     * @return
     */
    public List<ComplexityRating> findWithinOrgUnitTree(long orgUnitId) {
        List<ComplexityScore> connectionScores = connectionComplexityService.findWithinOrgUnit(orgUnitId);
        List<ComplexityScore> serverScores = serverComplexityService.findWithinOrgUnit(orgUnitId);
        List<ComplexityScore> capabilityScores = capabilityComplexityService.findWithinOrgUnit(orgUnitId);

        Map<Long, ComplexityScore> connectionScoresById = indexBy(s -> s.id(), connectionScores);
        Map<Long, ComplexityScore> serverScoresById = indexBy(s -> s.id(), serverScores);
        Map<Long, ComplexityScore> capabilityScoresById = indexBy(s -> s.id(), capabilityScores);

        Set<Long> appIds = union(
                serverScoresById.keySet(),
                connectionScoresById.keySet(),
                capabilityScoresById.keySet()
        );

        return appIds.stream()
                .map(appId -> ImmutableComplexityRating.builder()
                        .serverComplexity(maybeGet(serverScoresById, appId))
                        .connectionComplexity(maybeGet(connectionScoresById, appId))
                        .capabilityComplexity(maybeGet(capabilityScoresById, appId))
                        .build())
                .collect(Collectors.toList());

    }


}
