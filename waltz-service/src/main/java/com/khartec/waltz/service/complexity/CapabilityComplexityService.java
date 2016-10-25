package com.khartec.waltz.service.complexity;

import com.khartec.waltz.data.complexity.CapabilityComplexityDao;
import com.khartec.waltz.model.complexity.ComplexityKind;
import com.khartec.waltz.model.complexity.ComplexityScore;
import com.khartec.waltz.model.tally.Tally;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.model.complexity.ComplexityUtilities.tallyToComplexityScore;

@Service
public class CapabilityComplexityService {

    private final CapabilityComplexityDao capabilityComplexityDao;


    @Autowired
    public CapabilityComplexityService(CapabilityComplexityDao capabilityComplexityDao) {
        this.capabilityComplexityDao = capabilityComplexityDao;
    }


    public List<ComplexityScore> findByAppIdSelector(Select<Record1<Long>> idSelector) {
        Double baseline = capabilityComplexityDao.findBaseline();
        return findByAppIdSelector(idSelector, baseline);
    }


    public List<ComplexityScore> findByAppIdSelector(Select<Record1<Long>> idSelector, double baseline) {
        return capabilityComplexityDao.findScoresForAppIdSelector(idSelector)
                .stream()
                .map(tally -> tallyToComplexityScore(
                        ComplexityKind.CAPABILITY,
                        tally,
                        baseline))
                .collect(Collectors.toList());
    }


    public ComplexityScore getForApp(long appId) {
        double baseline = capabilityComplexityDao.findBaseline();
        return getForApp(appId, baseline);
    }


    public ComplexityScore getForApp(long appId, double baseline) {
        Tally<Long> tally = capabilityComplexityDao.findScoresForAppId(appId);
        if (tally == null) return null;

        return tallyToComplexityScore(
                ComplexityKind.CAPABILITY,
                tally,
                baseline);
    }

}
