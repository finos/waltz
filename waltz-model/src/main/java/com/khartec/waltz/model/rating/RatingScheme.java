package com.khartec.waltz.model.rating;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import org.immutables.value.Value;

import java.util.List;

import static com.khartec.waltz.common.ListUtilities.newArrayList;

@Value.Immutable
@JsonSerialize(as = ImmutableRatingScheme.class)
@JsonDeserialize(as = ImmutableRatingScheme.class)
public abstract class RatingScheme implements
        IdProvider,
        NameProvider,
        DescriptionProvider {

    @Value.Default
    public List<RagName> ratings() {
        return toList();
    }

    private static final RagName dfltR = ImmutableRagName.builder()
            .ratingSchemeId(1)
            .rating('R')
            .name("dflt - Disinvest")
            .description("dflt - Disinvest")
            .color("#d62728")
            .position(10)
            .needsPlannedDate(false)
            .build();


    private static final RagName dfltA = ImmutableRagName.builder()
            .ratingSchemeId(1)
            .rating('A')
            .name("dflt - Maintain")
            .description("dflt - Maintain")
            .color("#ff7f0e")
            .position(20)
            .needsPlannedDate(false)
            .build();


    private static final RagName dfltG = ImmutableRagName.builder()
            .ratingSchemeId(1)
            .rating('G')
            .name("dflt - Invest")
            .description("dflt - Invest")
            .color("#2ca02c")
            .position(30)
            .needsPlannedDate(false)
            .build();

    private static final RagName dfltT = ImmutableRagName.builder()
            .ratingSchemeId(1)
            .rating('F')
            .name("dflt - Future")
            .description("dflt - Future")
            .color("#786aa5")
            .position(35)
            .needsPlannedDate(true)
            .build();



    private static final RagName dfltZ = ImmutableRagName.builder()
            .ratingSchemeId(1)
            .rating('Z')
            .name("dflt - Unknown")
            .description("dflt - Unknown")
            .color("#28a1b6")
            .userSelectable(false)
            .position(40)
            .needsPlannedDate(false)
            .build();


    private static final RagName dfltX = ImmutableRagName.builder()
            .ratingSchemeId(1)
            .rating('X')
            .name("dflt - Not Applicable")
            .description("dflt - Not Applicable")
            .color("#eee")
            .position(50)
            .needsPlannedDate(false)
            .build();


    @Deprecated
    public static List<RagName> toList() {
        return newArrayList(
                dfltT,
                dfltR,
                dfltA,
                dfltG,
                dfltX,
                dfltZ);
    }

    public static RatingScheme mkDflt() {
        return ImmutableRatingScheme.builder()
                .id(1)
                .name("default")
                .description("default rating scheme")
                .build();
    }
}
