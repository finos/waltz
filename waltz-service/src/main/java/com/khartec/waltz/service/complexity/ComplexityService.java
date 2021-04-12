package com.khartec.waltz.service.complexity;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.GenericSelector;
import com.khartec.waltz.data.GenericSelectorFactory;
import com.khartec.waltz.data.complexity.ComplexityDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.complexity.Complexity;
import com.khartec.waltz.model.complexity.ComplexityKind;
import com.khartec.waltz.model.complexity.ComplexitySummary;
import com.khartec.waltz.model.complexity.ImmutableComplexitySummary;
import com.khartec.waltz.service.complexity_kind.ComplexityKindService;
import org.jooq.lambda.tuple.Tuple2;
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
                                                             int limit){

        GenericSelector genericSelector = genericSelectorFactory.applyForKind(targetKind, options);

        Set<Complexity> topComplexities = complexityDao.findTopComplexityScoresForKindAndSelector(complexityKindId, genericSelector, limit);

        Tuple2<BigDecimal, BigDecimal> averageAndTotalScore = complexityDao.getAverageAndTotalScoreforKindAndSelector(complexityKindId, genericSelector);

        Tuple2<Integer, Integer> mappedAndMissingCountsForKindBySelector = complexityDao.getMappedAndMissingCountsForKindBySelector(complexityKindId, genericSelector);

        ComplexityKind complexityKind = complexityKindService.getById(complexityKindId);

        return ImmutableComplexitySummary.builder()
                .complexityKind(complexityKind)
                .topComplexityScores(topComplexities)
                .average(averageAndTotalScore.v1.setScale(2, ROUND_HALF_UP))
                .total(averageAndTotalScore.v2)
                .mappedCount(mappedAndMissingCountsForKindBySelector.v1)
                .missingCount(mappedAndMissingCountsForKindBySelector.v2)
                .build();
    }

}
