package com.khartec.waltz.service.complexity;

import com.khartec.waltz.data.complexity.CapabilityComplexityDao;
import com.khartec.waltz.model.complexity.ComplexityScore;
import com.khartec.waltz.model.tally.LongTally;
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


    public List<ComplexityScore> findByAppIds(Long[] ids) {
        Double baseline = capabilityComplexityDao.findBaseline();
        return findByAppIds(ids, baseline);
    }


    public List<ComplexityScore> findByAppIds(Long[] ids, double baseline) {
        return capabilityComplexityDao.findScoresForAppIds(ids)
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

}
