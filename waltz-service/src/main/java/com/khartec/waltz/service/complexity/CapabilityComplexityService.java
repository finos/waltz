package com.khartec.waltz.service.complexity;

import com.khartec.waltz.data.complexity.CapabilityComplexityDao;
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
public class CapabilityComplexityService {

    private final OrganisationalUnitDao orgUnitDao;
    private final CapabilityComplexityDao capabilityComplexityDao;


    @Autowired
    public CapabilityComplexityService(OrganisationalUnitDao orgUnitDao, CapabilityComplexityDao capabilityComplexityDao) {
        this.orgUnitDao = orgUnitDao;
        this.capabilityComplexityDao = capabilityComplexityDao;
    }


    public List<ComplexityScore> findWithinOrgUnit(long orgUnitId) {
        double baseline = capabilityComplexityDao.findBaseline();
        return findWithinOrgUnit(orgUnitId, baseline);
    }


    public List<ComplexityScore> findWithinOrgUnit(long orgUnitId, double baseline) {

        Long[] orgUnitIds = toIdArray(orgUnitDao.findDescendants(orgUnitId));

        return capabilityComplexityDao.findScoresForOrgUnitIds(orgUnitIds)
                .stream()
                .map(tally -> tallyToComplexityScore(tally, baseline))
                .collect(Collectors.toList());
    }


    public ComplexityScore getForApp(long appId) {
        double baseline = capabilityComplexityDao.findBaseline();
        return getForApp(appId, baseline);
    }


    public ComplexityScore getForApp(long appId, double baseline) {
        LongTally tally = capabilityComplexityDao.findScoresForAppId(appId);
        if (tally == null) return null;

        return tallyToComplexityScore(tally, baseline);
    }


    // -- HELPERS ------

    private static Select<Record1<Long>> byOrgUnit(Long... orgUnits) {
        return DSL.select(APPLICATION.ID)
                .from(APPLICATION)
                .where(APPLICATION.ORGANISATIONAL_UNIT_ID.in(orgUnits));
    }

    private static Select<Record1<Long>> byOrgUnitTree(Long unitId, OrganisationalUnitDao ouDao) {
        return byOrgUnit();
    }

}
