package org.finos.waltz.test_common_again.helpers;

import org.finos.waltz.model.rating.ImmutableRatingSchemeItem;
import org.finos.waltz.schema.tables.records.RatingSchemeRecord;
import org.finos.waltz.service.rating_scheme.RatingSchemeService;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.lang.String.format;
import static org.finos.waltz.schema.Tables.RATING_SCHEME;

@Service
public class RatingSchemeHelper {

    @Autowired
    private DSLContext dsl;


    @Autowired
    private RatingSchemeService ratingSchemeService;

    public long createEmptyRatingScheme(String name) {
        return dsl
                .select(RATING_SCHEME.ID)
                .from(RATING_SCHEME)
                .where(RATING_SCHEME.NAME.eq(name))
                .fetchOptional(RATING_SCHEME.ID)
                .orElseGet(() -> {
                    RatingSchemeRecord record = dsl.newRecord(RATING_SCHEME);
                    record.setName(name);
                    record.setDescription(name);
                    record.store();
                    return record.getId();
                });
    }


    public Long saveRatingItem(long schemeId, String name, int position, String color, Character code) {
        ImmutableRatingSchemeItem rating = ImmutableRatingSchemeItem.builder()
                .name(name)
                .description(format("%s description", name))
                .ratingSchemeId(schemeId)
                .position(position)
                .color(color)
                .rating(code)
                .build();

        return ratingSchemeService.saveRatingItem(schemeId, rating);
    }
}
