package org.finos.waltz.model.legal_entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableLegalEntityRelKindStat.class)
@JsonDeserialize(as = ImmutableLegalEntityRelKindStat.class)
public abstract class LegalEntityRelKindStat {

    public abstract long relKindId();

    public abstract int targetEntityCount();

    public abstract int legalEntityCount();

    public abstract int relationshipCount();

}
