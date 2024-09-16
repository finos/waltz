package org.finos.waltz.model.bulk_upload.taxonomy;

public enum ValidationError {
    PARENT_NOT_FOUND,
    DUPLICATE_EXT_ID,
    CYCLE_DETECTED
}
