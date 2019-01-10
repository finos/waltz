package com.khartec.waltz.service.taxonomy_management.processors;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.measurable.Measurable;
import com.khartec.waltz.model.taxonomy_management.*;
import com.khartec.waltz.service.measurable.MeasurableService;
import com.khartec.waltz.service.measurable_rating.MeasurableRatingService;
import com.khartec.waltz.service.taxonomy_management.TaxonomyCommandProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.SetUtilities.asSet;
import static com.khartec.waltz.service.taxonomy_management.TaxonomyManagementUtilities.*;

@Service
public class UpdateMeasurableDescriptionCommandProcessor implements TaxonomyCommandProcessor {

    private final MeasurableService measurableService;
    private final MeasurableRatingService measurableRatingService;


    @Autowired
    public UpdateMeasurableDescriptionCommandProcessor(MeasurableService measurableService,
                                                       MeasurableRatingService measurableRatingService) {
        checkNotNull(measurableService, "measurableService cannot be null");
        checkNotNull(measurableRatingService, "measurableRatingService cannot be null");
        this.measurableService = measurableService;
        this.measurableRatingService = measurableRatingService;
    }


    @Override
    public Set<TaxonomyChangeType> supportedTypes() {
        return asSet(TaxonomyChangeType.UPDATE_DESCRIPTION);
    }


    @Override
    public EntityKind domain() {
        return EntityKind.MEASURABLE_CATEGORY;
    }


    public TaxonomyChangePreview preview(TaxonomyChangeCommand cmd) {
        doBasicValidation(cmd);
        Measurable m = validatePrimaryMeasurable(measurableService, cmd);

        ImmutableTaxonomyChangePreview.Builder preview = ImmutableTaxonomyChangePreview
                .builder()
                .command(ImmutableTaxonomyChangeCommand
                        .copyOf(cmd)
                        .withPrimaryReference(m.entityReference()));

        if (hasNoChange(m.description(), getDescriptionParam(cmd), "Description")) {
            return preview.build();
        }

        addToPreview(
                    preview,
                    findCurrentRatingMappings(measurableRatingService, cmd),
                    Severity.INFORMATION,
                    "Current app mappings exist to item, these may be misleading if the description change alters the meaning of this item");


        return preview.build();
    }


    public TaxonomyChangeCommand apply(TaxonomyChangeCommand cmd, String userId) {
        doBasicValidation(cmd);
        validatePrimaryMeasurable(measurableService, cmd);

        measurableService.updateDescription(
                cmd.primaryReference().id(),
                getDescriptionParam(cmd),
                userId);

        return ImmutableTaxonomyChangeCommand
                .copyOf(cmd)
                .withLastUpdatedAt(DateTimeUtilities.nowUtc())
                .withLastUpdatedBy(userId)
                .withStatus(TaxonomyChangeLifecycleStatus.EXECUTED);
    }
}
