package org.finos.waltz.service.complexity;

import org.finos.waltz.service.complexity_kind.ComplexityKindService;
import org.finos.waltz.common.Checks;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.complexity.ComplexityDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.complexity.*;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

import static java.math.BigDecimal.ROUND_HALF_UP;

@Service
public class ComplexityService {

    private final ComplexityDao complexityDao;
    private final ComplexityKindService complexityKindService;
    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();


    @Autowired
    ComplexityService(ComplexityDao complexityDao, ComplexityKindService complexityKindService) {
        Checks.checkNotNull(complexityDao, "complexityDao cannot be null");
        Checks.checkNotNull(complexityKindService, "complexityKindService cannot be null");
        this.complexityDao = complexityDao;
        this.complexityKindService = complexityKindService;
    }


    public Set<Complexity> findByEntityReference(EntityReference ref){
        return complexityDao.findByEntityReference(ref);
    }


    public Set<Complexity> findBySelector(EntityKind targetKind, IdSelectionOptions options){

        GenericSelector genericSelector = genericSelectorFactory.applyForKind(targetKind, options);

        return complexityDao.findBySelector(genericSelector);
    }


    public ComplexitySummary getComplexitySummaryForSelector(Long complexityKindId,
                                                             EntityKind targetKind,
                                                             IdSelectionOptions options,
                                                             int limit) {

        GenericSelector genericSelector = genericSelectorFactory.applyForKind(targetKind, options);

        Set<Complexity> topComplexities = complexityDao.findTopComplexityScoresForKindAndSelector(complexityKindId, genericSelector, limit);

        Tuple3<BigDecimal, BigDecimal, BigDecimal> averageAndTotalAndMedianScore = complexityDao.getAverageAndTotalAndMedianScoreByKindAndSelector(complexityKindId, genericSelector);

        Tuple2<Integer, Integer> mappedAndMissingCountsForKindBySelector = complexityDao.getMappedAndMissingCountsForKindBySelector(complexityKindId, genericSelector);

        Tuple2<BigDecimal, BigDecimal> standardDeviationAndVariance = complexityDao.getStandardDeviationAndVariance(complexityKindId, genericSelector, averageAndTotalAndMedianScore.v1, mappedAndMissingCountsForKindBySelector.v1);

        ComplexityKind complexityKind = complexityKindService.getById(complexityKindId);

        return ImmutableComplexitySummary.builder()
                .complexityKind(complexityKind)
                .topComplexityScores(topComplexities)
                .average(averageAndTotalAndMedianScore.v1.setScale(2, ROUND_HALF_UP))
                .median(averageAndTotalAndMedianScore.v3.setScale(2, ROUND_HALF_UP))
                .variance(standardDeviationAndVariance.v2.setScale(2, ROUND_HALF_UP))
                .standardDeviation(standardDeviationAndVariance.v1.setScale(2, ROUND_HALF_UP))
                .total(averageAndTotalAndMedianScore.v2)
                .mappedCount(mappedAndMissingCountsForKindBySelector.v1)
                .missingCount(mappedAndMissingCountsForKindBySelector.v2)
                .build();
    }


    public Set<ComplexityTotal> findTotalsByTargetKindAndSelector(EntityKind targetKind,
                                                                  IdSelectionOptions selectionOptions) {
        GenericSelector genericSelector = genericSelectorFactory.applyForKind(targetKind, selectionOptions);

        return complexityDao.findTotalsByGenericSelector(genericSelector);
    }
}
