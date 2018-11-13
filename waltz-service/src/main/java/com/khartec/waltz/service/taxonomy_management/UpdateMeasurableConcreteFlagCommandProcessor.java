package com.khartec.waltz.service.taxonomy_management;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.common.LoggingUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.measurable.Measurable;
import com.khartec.waltz.model.measurable_rating.MeasurableRating;
import com.khartec.waltz.model.taxonomy_management.*;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.measurable.MeasurableService;
import com.khartec.waltz.service.measurable_rating.MeasurableRatingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;
import static com.khartec.waltz.common.ListUtilities.map;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;

@Service
public class UpdateMeasurableConcreteFlagCommandProcessor implements TaxonomyCommandProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateMeasurableConcreteFlagCommandProcessor.class);

    private final MeasurableService measurableService;
    private final MeasurableRatingService measurableRatingService;


    @Autowired
    public UpdateMeasurableConcreteFlagCommandProcessor(MeasurableService measurableService,
                                                        MeasurableRatingService measurableRatingService) {
        checkNotNull(measurableService, "measurableService cannot be null");
        checkNotNull(measurableRatingService, "measurableRatingService cannot be null");
        this.measurableService = measurableService;
        this.measurableRatingService = measurableRatingService;
    }


    public TaxonomyChangePreview preview(TaxonomyChangeCommand cmd) {
        doBasicValidation(cmd);
        Measurable m = validateMeasurable(cmd);

        ImmutableTaxonomyChangePreview.Builder preview = ImmutableTaxonomyChangePreview
                .builder()
                .command(ImmutableTaxonomyChangeCommand
                        .copyOf(cmd)
                        .withA(m.entityReference()));

        boolean newValue = cmd.valueAsBoolean();

        if (hasNoChange(m, newValue)) {
            return preview.build();
        }

        if (newValue == false) {
            calcCurrentMappings(cmd)
                .map(preview::addImpacts);
        }
        return preview.build();
    }


    @Override
    public TaxonomyChangeType type() {
        return TaxonomyChangeType.UPDATE_CONCRETENESS;
    }


    public TaxonomyChangeCommand apply(TaxonomyChangeCommand cmd, String userId) {
        doBasicValidation(cmd);
        validateMeasurable(cmd);

        measurableService.updateConcreteFlag(
                cmd.a().id(),
                cmd.valueAsBoolean());

        return ImmutableTaxonomyChangeCommand
                .copyOf(cmd)
                .withExecutedAt(DateTimeUtilities.nowUtc())
                .withExecutedBy(userId)
                .withStatus(TaxonomyChangeLifecycleStatus.EXECUTED);
    }


    // --- helpers

    private boolean hasNoChange(Measurable m, boolean newValue) {
        boolean currentState = m.concrete();
        if (currentState == newValue) {
            LOG.info("Aborting command as nothing to do, concrete flag is already {}", newValue);
            return true;
        } else {
            return false;
        }
    }


    private Optional<TaxonomyChangeImpact> calcCurrentMappings(TaxonomyChangeCommand cmd) {
        IdSelectionOptions selectionOptions = mkOpts(cmd.a(), HierarchyQueryScope.EXACT);
        List<MeasurableRating> ratings = measurableRatingService.findByMeasurableIdSelector(selectionOptions);
        return ratings.isEmpty()
                ? Optional.empty()
                : Optional.of(ImmutableTaxonomyChangeImpact.builder()
                        .severity(Severity.WARNING)
                        .description("Current app mappings exist to item, these will be invalid when the item becomes non-concrete")
                        .impactedReferences(map(ratings, r -> r.entityReference()))
                        .build());
    }


    private Measurable validateMeasurable(TaxonomyChangeCommand cmd) {
        long measurableId = cmd.a().id();
        Measurable measurable = measurableService.getById(measurableId);

        checkNotNull(
                measurable,
                "Cannot find measurable [%d]",
                measurableId);

        checkTrue(
                cmd.changeDomain().id() == measurable.categoryId(),
                "Measurable [%s / %d] is not in category [%d], instead it is in category [%d]",
                measurable.name(),
                measurable.id(),
                cmd.changeDomain().id(),
                measurable.categoryId());

        return measurable;
    }


    private void doBasicValidation(TaxonomyChangeCommand cmd) {
        cmd.validate();
        checkDomain(cmd, EntityKind.MEASURABLE_CATEGORY);
        checkType(cmd, TaxonomyChangeType.UPDATE_CONCRETENESS);
    }


    // --- test rig

    public static void main(String[] args) throws IOException {
        LoggingUtilities.configureLogging();
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        UpdateMeasurableConcreteFlagCommandProcessor proc = ctx.getBean(UpdateMeasurableConcreteFlagCommandProcessor.class);

        TaxonomyChangeCommand cmd = ImmutableTaxonomyChangeCommand.builder()
                .changeType(TaxonomyChangeType.UPDATE_CONCRETENESS)
                .changeDomain(mkRef(EntityKind.MEASURABLE_CATEGORY, 1))
                .a(mkRef(EntityKind.MEASURABLE, 630))
                .newValue(Boolean.FALSE.toString())
                .createdBy("admin")
                .build();

        TaxonomyChangePreview preview = proc.preview(cmd);
        System.out.printf("Preview: %s\n", preview);
        proc.apply(cmd, "admin");
    }

}
