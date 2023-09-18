package org.finos.waltz.service.complexity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.finos.waltz.common.Checks;
import org.finos.waltz.common.JacksonUtilities;
import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.complexity.ComplexityDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.complexity.Complexity;
import org.finos.waltz.model.complexity.ComplexityKind;
import org.finos.waltz.model.complexity.ComplexitySummary;
import org.finos.waltz.model.complexity.ComplexityTotal;
import org.finos.waltz.model.complexity.ImmutableComplexitySummary;
import org.finos.waltz.model.complexity.MeasurableComplexityDetail;
import org.finos.waltz.model.scheduled_job.JobKey;
import org.finos.waltz.model.settings.Setting;
import org.finos.waltz.service.complexity_kind.ComplexityKindService;
import org.finos.waltz.service.settings.SettingsService;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.math.BigDecimal.ROUND_HALF_UP;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;
import static org.finos.waltz.model.settings.NamedSettings.mkScheduledJobParamSetting;

@Service
public class ComplexityService {

    private static final Logger LOG = LoggerFactory.getLogger(ComplexityService.class);
    private final ComplexityDao complexityDao;
    private final ComplexityKindService complexityKindService;
    
    private final SettingsService settingsService;
    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();


    @Autowired
    ComplexityService(ComplexityDao complexityDao, 
                      ComplexityKindService complexityKindService, 
                      SettingsService settingsService) {
        
        Checks.checkNotNull(complexityDao, "complexityDao cannot be null");
        Checks.checkNotNull(complexityKindService, "complexityKindService cannot be null");
        Checks.checkNotNull(settingsService, "settingsService cannot be null");
        
        this.complexityDao = complexityDao;
        this.complexityKindService = complexityKindService;
        this.settingsService = settingsService;
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


    public void populateMeasurableComplexities() {
        String jobKey = mkScheduledJobParamSetting(JobKey.COMPLEXITY_REBUILD_MEASURABLE);
        Setting params = settingsService.getByName(jobKey);

        if (params != null) {
            LOG.info("Reading measurable complexity job params from setting");
            ObjectMapper mapper = JacksonUtilities.getJsonMapper();

            List<MeasurableComplexityDetail> measurableComplexityDetails = params.value()
                    .map(paramString -> {
                        try {
                            MeasurableComplexityDetail[] details = mapper.readValue(paramString, MeasurableComplexityDetail[].class);
                            return ListUtilities.asList(details);
                        } catch (IOException e) {
                            throw new RuntimeException("Unable to parse parameter from settings value", e);
                        }
                    })
                    .orElse(emptyList());

            Map<String, Long> complexityKindByExternalId = complexityKindService.findAll()
                    .stream()
                    .filter(d -> d.externalId().isPresent())
                    .collect(toMap(d -> d.externalId().get(), d -> d.id().get()));

            LOG.info("Starting processing measurable complexities from settings");

            measurableComplexityDetails
                    .forEach(detail -> complexityDao.processMeasurableComplexity(detail, complexityKindByExternalId));

            LOG.info("Completed processing measurable complexities from settings");

        } else {
            LOG.info(format("No setting value for job key: %s", jobKey));
        }
    }
}
