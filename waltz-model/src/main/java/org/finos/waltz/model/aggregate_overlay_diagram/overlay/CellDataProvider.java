package org.finos.waltz.model.aggregate_overlay_diagram.overlay;

import java.util.Set;

public interface CellDataProvider<T extends CellExternalIdProvider> {

    Set<T> cellData();

}
