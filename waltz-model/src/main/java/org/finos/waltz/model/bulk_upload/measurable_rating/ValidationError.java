package org.finos.waltz.model.bulk_upload.measurable_rating;

public enum ValidationError {
    MEASURABLE_NOT_FOUND,
    APPLICATION_NOT_FOUND,
    RATING_NOT_FOUND,
    MEASURABLE_NOT_CONCRETE,
    RATING_NOT_USER_SELECTABLE,
    ALLOCATION_EXCEEDING,
    ALLOCATION_NOT_VALID,
    MEASURABLES_MISMATCH
}
