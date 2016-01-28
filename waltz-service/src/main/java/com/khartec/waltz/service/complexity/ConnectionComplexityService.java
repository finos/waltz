package com.khartec.waltz.service.complexity;

import com.khartec.waltz.data.complexity.ConnectionComplexityDao;
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.model.complexity.ComplexityScore;
import com.khartec.waltz.model.tally.LongTally;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.model.complexity.ComplexityUtilities.tallyToComplexityScore;
import static com.khartec.waltz.model.utils.IdUtilities.toIdArray;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;

@Service
public class ConnectionComplexityService {

    private final OrganisationalUnitDao orgUnitDao;
    private final ConnectionComplexityDao connectionComplexityDao;


    @Autowired
    public ConnectionComplexityService(ConnectionComplexityDao connectionComplexityDao, OrganisationalUnitDao orgUnitDao) {
        this.connectionComplexityDao = connectionComplexityDao;
        this.orgUnitDao = orgUnitDao;
    }


    /**
     * Finds the connection complexity for a given app.  It uses a
     * default baseline derived from looking at the app with the
     * most connections in the system.
     * @param appId
     * @return
     */
    public ComplexityScore getForApp(long appId) {
        int baseline = connectionComplexityDao.findBaseline();
        return getForApp(appId, baseline);
    }


    /**
     * Calculates the connection complexity for a given app.
     * It uses the supplied baseline value for calculating the complexity
     * score.
     * @param appId
     * @param baseline
     * @return
     */
    public ComplexityScore getForApp(long appId, int baseline) {
        List<LongTally> flowCounts = connectionComplexityDao.findCounts(appId);

        if (flowCounts.isEmpty()) { return null; }

        return tallyToComplexityScore(flowCounts.get(0), baseline, Math::log);
    }


    /**
     * Find connection complexity of applications within a given organisational unit (and it's
     * sub units).  The complexity are baselined against the application with the most
     * connections in the system.  If you wish specify a specific baseline use
     * the overloaded method.
     * @param orgUnitId
     * @return
     */
    public List<ComplexityScore> findWithinOrgUnit(long orgUnitId) {
        int baseline = connectionComplexityDao.findBaseline();
        return findWithinOrgUnit(orgUnitId, baseline);
    }


    /**
     * Find connection complexity of application within the given org unit (and it's sub
     * units).  Complexity is baselined against the given value.
     * @param orgUnitId
     * @param baseline
     * @return
     */
    public List<ComplexityScore> findWithinOrgUnit(long orgUnitId, int baseline) {
        return connectionComplexityDao.findCounts(byOrgUnitTree(orgUnitId, orgUnitDao))
                .stream()
                .map(tally -> tallyToComplexityScore(tally, baseline, Math::log))
                .collect(Collectors.toList());
    }


    // -- HELPERS ----

    private static Select<Record1<Long>> byOrgUnit(Long... orgUnits) {
        return DSL.select(APPLICATION.ID)
                .from(APPLICATION)
                .where(APPLICATION.ORGANISATIONAL_UNIT_ID.in(orgUnits));
    }


    private static Select<Record1<Long>> byOrgUnitTree(Long unitId, OrganisationalUnitDao ouDao) {
        return byOrgUnit(toIdArray(ouDao.findDescendants(unitId)));
    }
}
