package org.finos.waltz.model.user;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;

import static org.finos.waltz.common.StringUtilities.isEmpty;

@Value.Immutable
@JsonSerialize(as = ImmutableBulkUserOperationRowPreview.class)
public abstract class BulkUserOperationRowPreview {

    public enum ResolutionStatus {
        OK,
        ERROR
    }


    @Nullable
    public abstract String givenUser();

    @Nullable
    public abstract String givenRole();
    @Nullable
    public abstract String givenComment();

    @Nullable
    public abstract String resolvedUser();

    @Nullable
    public abstract String resolvedRole();

    @Nullable
    public abstract String resolvedComment();
    @Value.Derived
    public ResolutionStatus status() {
        return isEmpty(resolvedUser()) || isEmpty(resolvedRole()) ||  isEmpty(resolvedComment())
                ? ResolutionStatus.ERROR
                : ResolutionStatus.OK;
    }



}
