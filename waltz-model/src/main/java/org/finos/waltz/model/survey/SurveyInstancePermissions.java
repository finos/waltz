package org.finos.waltz.model.survey;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyInstancePermissions.class)
@JsonDeserialize(as = ImmutableSurveyInstancePermissions.class)
public abstract class SurveyInstancePermissions {
    @Value.Default
    public boolean isAdmin() {
        return false;
    }

    @Value.Default
    public boolean isOwner() {
        return false;
    }

    @Value.Default
    public boolean hasOwnerRole() {
        return false;
    }

    public boolean hasOwnership() {
        return isOwner() || hasOwnerRole();
    }

    @Value.Default
    public boolean isParticipant() {
        return false;
    }

    /**
     * Indicates if the user can edit meta data associated with this survey instance
     * (e.g. recipients, due dates etc)
     *
     * Derived via a rule similar to: `isLatest && (isAdmin || isOwner || hasOwningRole)`
     *
     * @return true if user can edit associated metadata
     */
    @Value.Default
    public boolean isMetaEdit() {
        return false;
    }

    @Value.Default
    public boolean canEdit() {
        return false;
    }
}
