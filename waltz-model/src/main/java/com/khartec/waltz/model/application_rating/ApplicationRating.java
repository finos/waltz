package com.khartec.waltz.model.application_rating;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.Nullable;
import com.khartec.waltz.model.ProvenanceProvider;
import com.khartec.waltz.model.capabilityrating.RagRating;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableApplicationRating.class)
@JsonDeserialize(as = ImmutableApplicationRating.class)
public abstract class ApplicationRating implements
        DescriptionProvider,
        LastUpdatedProvider,
        ProvenanceProvider {

    public abstract long applicationId();

    @Nullable
    public abstract Long capabilityId();

    @Nullable
    public abstract Long processId();

    @Nullable
    public abstract Long regionId();

    @Nullable
    public abstract Long businessLineId();

    @Nullable
    public abstract Long productId();

    public abstract RagRating rating();

    public abstract int weight();


    /**
     * How many perspectives are known
     * @return
     */
    @Value.Derived
    public int perspectiveCount() {
        int perspectiveCount = 0;

        if (capabilityId() != null) perspectiveCount++;
        if (processId() != null) perspectiveCount++;
        if (regionId() != null) perspectiveCount++;
        if (businessLineId() != null) perspectiveCount++;
        if (productId() != null) perspectiveCount++;

        return perspectiveCount;
    }
}
