package org.finos.waltz.model.bulk_upload;

public enum BulkUpdateMode {
    ADD_ONLY,  // will not remove, only add and/or update
    REPLACE // will remove things
}
