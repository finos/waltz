package com.khartec.waltz.service.taxonomy_management.processors;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.measurable.Measurable;
import com.khartec.waltz.model.taxonomy_management.*;
import com.khartec.waltz.service.measurable.MeasurableService;
import com.khartec.waltz.service.measurable_rating.MeasurableRatingService;
import com.khartec.waltz.service.taxonomy_management.TaxonomyCommandProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.service.taxonomy_management.TaxonomyManagementUtilities.*;

@Service
public class UpdateMeasurableExternalIdCommandProcessor implements TaxonomyCommandProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateMeasurableExternalIdCommandProcessor.class);

    private final MeasurableService measurableService;


    @Autowired
    public UpdateMeasurableExternalIdCommandProcessor(MeasurableService measurableService) {
        checkNotNull(measurableService, "measurableService cannot be null");
        this.measurableService = measurableService;
    }


    @Override
    public TaxonomyChangeType type() {
        return TaxonomyChangeType.UPDATE_EXTERNAL_ID;
    }

    @Override
    public EntityKind domain() {
        return EntityKind.MEASURABLE_CATEGORY;
    }


    public TaxonomyChangePreview preview(TaxonomyChangeCommand cmd) {
        doBasicValidation(cmd);
        Measurable m = validateMeasurable(measurableService, cmd);
        return ImmutableTaxonomyChangePreview
                .builder()
                .command(ImmutableTaxonomyChangeCommand
                        .copyOf(cmd)
                        .withA(m.entityReference()))
                .build();
    }


    public TaxonomyChangeCommand apply(TaxonomyChangeCommand cmd, String userId) {
        doBasicValidation(cmd);
        validateMeasurable(measurableService, cmd);

        measurableService.updateExternalId(
                cmd.a().id(),
                cmd.newValue(),
                userId);

        return ImmutableTaxonomyChangeCommand
                .copyOf(cmd)
                .withExecutedAt(DateTimeUtilities.nowUtc())
                .withExecutedBy(userId)
                .withStatus(TaxonomyChangeLifecycleStatus.EXECUTED);
    }

}
