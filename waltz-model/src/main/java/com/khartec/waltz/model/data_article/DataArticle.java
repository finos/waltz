package com.khartec.waltz.model.data_article;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import org.immutables.value.Value;

/**
 * Represents the a particular type of type of file / message / byte array
 * produced by an owning system. This format is stored separately from the
 * physical flow to better represent systems which distribute a single
 * file to numerous consumers.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableDataArticle.class)
@JsonDeserialize(as = ImmutableDataArticle.class)
public abstract class DataArticle implements
        IdProvider,
        ExternalIdProvider,
        NameProvider,
        DescriptionProvider,
        ProvenanceProvider {

    public abstract long owningApplicationId();
    public abstract DataFormatKind format();

}
