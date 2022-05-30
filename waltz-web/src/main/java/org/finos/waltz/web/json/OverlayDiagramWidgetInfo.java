package org.finos.waltz.web.json;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.AssessmentBasedSelectionFilter;
import org.finos.waltz.model.IdSelectionOptions;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableOverlayDiagramWidgetInfo.class)
@JsonDeserialize(as = ImmutableOverlayDiagramWidgetInfo.class)
public abstract class OverlayDiagramWidgetInfo {

    public abstract IdSelectionOptions idSelectionOptions();

    public abstract Optional<AssessmentBasedSelectionFilter> assessmentBasedSelectionFilter();

}


