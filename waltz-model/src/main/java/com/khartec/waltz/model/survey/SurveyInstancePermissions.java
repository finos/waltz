package com.khartec.waltz.model.survey;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.person.Person;
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
    @Value.Default
    public boolean isMetaEdit() {
        return false;
    }
}
