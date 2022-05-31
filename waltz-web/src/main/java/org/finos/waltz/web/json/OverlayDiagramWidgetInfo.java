package org.finos.waltz.web.json;


import org.finos.waltz.model.AssessmentBasedSelectionFilter;
import org.finos.waltz.model.IdSelectionOptions;

import java.util.Optional;

public abstract class OverlayDiagramWidgetInfo<T> {

    public abstract IdSelectionOptions idSelectionOptions();

    public abstract Optional<AssessmentBasedSelectionFilter> assessmentBasedSelectionFilter();

    public abstract T overlayParameters();

}


