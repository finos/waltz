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
public class UpdateMeasurableNameCommandProcessor implements TaxonomyCommandProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateMeasurableNameCommandProcessor.class);

    private final MeasurableService measurableService;
    private final MeasurableRatingService measurableRatingService;


    @Autowired
    public UpdateMeasurableNameCommandProcessor(MeasurableService measurableService,
                                                MeasurableRatingService measurableRatingService) {
        checkNotNull(measurableService, "measurableService cannot be null");
        checkNotNull(measurableRatingService, "measurableRatingService cannot be null");
        this.measurableService = measurableService;
        this.measurableRatingService = measurableRatingService;
    }


    @Override
    public TaxonomyChangeType type() {
        return TaxonomyChangeType.UPDATE_NAME;
    }

    @Override
    public EntityKind domain() {
        return EntityKind.MEASURABLE_CATEGORY;
    }


    public TaxonomyChangePreview preview(TaxonomyChangeCommand cmd) {
        doBasicValidation(cmd);
        Measurable m = validateMeasurable(measurableService, cmd);

        ImmutableTaxonomyChangePreview.Builder preview = ImmutableTaxonomyChangePreview
                .builder()
                .command(ImmutableTaxonomyChangeCommand
                        .copyOf(cmd)
                        .withA(m.entityReference()));

        if (hasNoChange(m.name(), cmd.newValue(), "Name")) {
            return preview.build();
        }

        addToPreview(
                    preview,
                    findCurrentRatingMappings(measurableRatingService, cmd),
                    Severity.INFORMATION,
                    "Current app mappings exist to item, these may be misleading if the name change alters the meaning of this item");

        return preview.build();
    }


    public TaxonomyChangeCommand apply(TaxonomyChangeCommand cmd, String userId) {
        doBasicValidation(cmd);
        validateMeasurable(measurableService, cmd);

        measurableService.updateName(
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
