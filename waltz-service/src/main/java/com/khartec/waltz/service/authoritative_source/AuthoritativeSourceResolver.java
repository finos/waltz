package com.khartec.waltz.service.authoritative_source;

import com.khartec.waltz.common.CollectionUtilities;
import com.khartec.waltz.common.MapUtilities;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.authoritativesource.AuthoritativeRatingVantagePoint;
import com.khartec.waltz.model.authoritativesource.Rating;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.MapUtilities.groupAndThen;


public class AuthoritativeSourceResolver {

    private final Map<EntityReference, Map<String, Map<Long, Optional<AuthoritativeRatingVantagePoint>>>> byOuThenDataTypeThenApp;


    /**
     * Construct the Resolver with an internal structure as follows:
     * OrgUnit -> [DataType -> [AppId -> Rating] ]
     * @param authoritativeRatingVantagePoints
     */
    public AuthoritativeSourceResolver(List<AuthoritativeRatingVantagePoint> authoritativeRatingVantagePoints) {
        checkNotNull(authoritativeRatingVantagePoints, "authoritativeRatingVantagePoints cannot be null");

        byOuThenDataTypeThenApp =
                groupAndThen(
                    as -> as.vantagePoint(),
                        byOus -> groupAndThen(
                                byOu -> byOu.dataTypeCode(),
                                byDts -> groupAndThen(
                                        dt -> dt.applicationId(),
                                        a -> getBestRanked(a),
                                        byDts),
                                byOus),
                    authoritativeRatingVantagePoints);

    }



    public Rating resolve(EntityReference vantagePoint, EntityReference source, String dataTypeCode) {

        Map<String, Map<Long, Optional<AuthoritativeRatingVantagePoint>>> ouGroup = byOuThenDataTypeThenApp.get(vantagePoint);
        if(MapUtilities.isEmpty(ouGroup)) return Rating.NO_OPINION;

        Map<Long, Optional<AuthoritativeRatingVantagePoint>> dataTypeGroup = ouGroup.get(dataTypeCode);
        if(MapUtilities.isEmpty(dataTypeGroup)) return Rating.NO_OPINION;

        Optional<AuthoritativeRatingVantagePoint> maybeRating = dataTypeGroup.getOrDefault(source.id(), Optional.empty());
        return maybeRating
                .map(r -> r.rating())
                .orElse(Rating.DISCOURAGED);
    }



    static Optional<AuthoritativeRatingVantagePoint> getBestRanked(Collection<AuthoritativeRatingVantagePoint> a) {
        return CollectionUtilities.head(CollectionUtilities.sort(a,
                (x, y) -> Integer.compare(y.rank(), x.rank())));
    }


}
