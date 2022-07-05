package org.finos.waltz.web.json;


import org.finos.waltz.model.AssessmentBasedSelectionFilter;
import org.finos.waltz.model.IdSelectionOptions;

import java.util.Optional;
import java.util.Set;

public abstract class OverlayDiagramWidgetInfo<T> {

    public abstract IdSelectionOptions idSelectionOptions();

    public abstract Set<AssessmentBasedSelectionFilter> assessmentBasedSelectionFilters();

    public abstract T overlayParameters();

}


