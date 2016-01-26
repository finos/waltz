package com.khartec.waltz.service.complexity;

import com.khartec.waltz.data.complexity.ServerComplexityDao;
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.model.complexity.ComplexityScore;
import com.khartec.waltz.model.tally.LongTally;
import org.jooq.Condition;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.model.complexity.ComplexityUtilities.tallyToComplexityScore;
import static com.khartec.waltz.model.utils.IdUtilities.toIdArray;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;

@Service
public class ServerComplexityService {

    private final OrganisationalUnitDao orgUnitDao;
    private final ServerComplexityDao serverComplexityDao;


    @Autowired
    public ServerComplexityService(OrganisationalUnitDao orgUnitDao, ServerComplexityDao serverComplexityDao) {
        this.orgUnitDao = orgUnitDao;
        this.serverComplexityDao = serverComplexityDao;
    }


    public List<ComplexityScore> findWithinOrgUnit(long orgUnitId) {
        int baseline = serverComplexityDao.findBaseline();
        return findWithinOrgUnit(orgUnitId, baseline);
    }


    public List<ComplexityScore> findWithinOrgUnit(long orgUnitId, int baseline) {
        Long[] orgIds = calcOrgUnitIds(orgUnitId, orgUnitDao);
        Condition orgTreePredicate = APPLICATION.ORGANISATIONAL_UNIT_ID.in(orgIds);

        return serverComplexityDao.findCountsByAppIds(orgTreePredicate)
                .stream()
                .map(tally -> tallyToComplexityScore(tally, baseline))
                .collect(Collectors.toList());
    }


    public ComplexityScore getForApp(long appId) {
        int baseline = serverComplexityDao.findBaseline();
        return getForApp(appId, baseline);
    }


    public ComplexityScore getForApp(long appId, int baseline) {
        List<LongTally> tallies = serverComplexityDao.findCountsByAppIds(appId);
        if (tallies.isEmpty()) { return null; }

        return tallyToComplexityScore(tallies.get(0), baseline);
    }


    // -- HELPERS ------

    private static Long[] calcOrgUnitIds(Long unitId, OrganisationalUnitDao ouDao) {
        return toIdArray(ouDao.findDescendants(unitId));
    }

}
