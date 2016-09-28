package com.khartec.waltz.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;


/**
 * A leveled entity reference is a lightweight object
 * which represents an entity reference and it's level
 * within it's entity hierarchy.  Typical uses are for
 * sending back a simplistic subset of a hierarchy (i.e.
 * immediate parents/children)
 */
@Value.Immutable
@JsonSerialize(as = ImmutableLeveledEntityReference.class)
@JsonDeserialize(as = ImmutableLeveledEntityReference.class)
public abstract class LeveledEntityReference {

    public abstract EntityReference entityReference();
    public abstract int level();

}
