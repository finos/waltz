package org.finos.waltz.model.measurable_rating;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.common.CollectionUtilities;
import org.finos.waltz.model.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommission;
import org.finos.waltz.model.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommissionInfo;
import org.finos.waltz.model.measurable_rating_replacement.MeasurableRatingReplacement;
import org.immutables.value.Value;

import java.util.Set;

import static org.finos.waltz.common.CollectionUtilities.isEmpty;

@Value.Immutable
@JsonSerialize(as = ImmutableDecommissionsView.class)
public interface DecommissionsView {

    Set<MeasurableRatingPlannedDecommission> plannedDecommissions();

    Set<MeasurableRatingPlannedDecommissionInfo> replacingDecommissions();

    Set<MeasurableRatingReplacement> plannedReplacements();


}
