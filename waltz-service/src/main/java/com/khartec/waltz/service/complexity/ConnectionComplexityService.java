package com.khartec.waltz.service.complexity;

import com.khartec.waltz.data.complexity.ConnectionComplexityDao;
import com.khartec.waltz.model.complexity.ComplexityKind;
import com.khartec.waltz.model.complexity.ComplexityScore;
import com.khartec.waltz.model.tally.LongTally;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.model.complexity.ComplexityUtilities.tallyToComplexityScore;

@Service
public class ConnectionComplexityService {

    private final ConnectionComplexityDao connectionComplexityDao;


    @Autowired
    public ConnectionComplexityService(ConnectionComplexityDao connectionComplexityDao) {
        this.connectionComplexityDao = connectionComplexityDao;
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

        return tallyToComplexityScore(
                ComplexityKind.CONNECTION,
                flowCounts.get(0),
                baseline,
                Math::log);
    }


    /**
     * Find connection complexity of the given applications. The complexity
     * ratings are baselined against the application with the most
     * connections in the system.  If you wish specify a specific baseline use
     * the overloaded method.
     * @param idSelector
     * @return
     */
    public List<ComplexityScore> findByAppIdSelector(Select<Record1<Long>> idSelector) {
        int baseline = connectionComplexityDao.findBaseline();
        return findByAppIdSelector(idSelector, baseline);
    }


    public List<ComplexityScore> findByAppIdSelector(Select<Record1<Long>> idSelector, int baseline) {
        return connectionComplexityDao.findCounts(idSelector)
                .stream()
                .map(tally -> tallyToComplexityScore(ComplexityKind.CONNECTION, tally, baseline, Math::log))
                .collect(Collectors.toList());
    }

}
