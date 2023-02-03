package org.finos.waltz.jobs.generators;

import org.finos.waltz.common.ArrayUtilities;
import org.finos.waltz.jobs.generators.model.Country;
import org.finos.waltz.jobs.generators.model.ImmutableCountry;
import org.finos.waltz.model.Cardinality;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.AssessmentRating;
import org.finos.waltz.schema.tables.LegalEntityRelationship;
import org.finos.waltz.schema.tables.RatingScheme;
import org.finos.waltz.schema.tables.RatingSchemeItem;
import org.finos.waltz.schema.tables.records.AssessmentDefinitionRecord;
import org.finos.waltz.schema.tables.records.AssessmentRatingRecord;
import org.finos.waltz.schema.tables.records.LegalEntityRecord;
import org.finos.waltz.schema.tables.records.LegalEntityRelationshipKindRecord;
import org.finos.waltz.schema.tables.records.LegalEntityRelationshipRecord;
import org.finos.waltz.schema.tables.records.RatingSchemeItemRecord;
import org.finos.waltz.schema.tables.records.RatingSchemeRecord;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple3;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.IOUtilities.streamLines;
import static org.finos.waltz.common.RandomUtilities.randomPick;
import static org.finos.waltz.common.RandomUtilities.randomlySizedIntStream;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.schema.Tables.*;
import static org.finos.waltz.schema.Tables.RATING_SCHEME;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class LegalEntityGenerator implements SampleDataGenerator {

    public static final String LE_REGION_AD_EXT_ID = "LE_REGION";
    public static final String LE_JURISDICTION_AD_EXT_ID = "LE_JURISDICTION";
    public static final String LE_REGION_RS_EXT_ID = "LE_REGION";
    public static final String COUNTRY_RS_EXT_ID = "COUNTRY";
    public static final String LE_APP_REGION_AD_EXT_ID = "LE_APP_REGION_AD_EXT_ID";
    private final Set<Country> countries = loadCountries();

    private final Set<String> services = asSet(
            "Consulting",
            "Legal Services",
            "Audit",
            "Assurance Services",
            "Information Technology Services",
            "Recovery Services",
            "Holdings",
            "Tax and Advisory");

    private final Set<String> suffixes = asSet(
            "LLP",
            "LLC",
            "Ltd",
            "Corp",
            "Incorporated",
            "Trust");

    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {
        DSLContext dsl = getDsl(ctx);
        AtomicInteger ctr = new AtomicInteger(1);
        long relKindId = mkRelKind(dsl);

        Map<String, Long> countryCodeToRatingId = createCountryRatingScheme(dsl);
        long countryAssessmentId = createCountryAssessment(dsl);
        long regionAssessmentId = createRegionAssessment(dsl);

        createLegalEntityRecords(dsl, countryAssessmentId, countryCodeToRatingId, ctr);
        createLegalEntityRelationships(dsl, ctr, relKindId);
        createRegionRatings(dsl, regionAssessmentId);


        return null;
    }

    private void createRegionRatings(DSLContext dsl, long regionAssessmentId) {

        LegalEntityRelationship ler = LEGAL_ENTITY_RELATIONSHIP;
        RatingSchemeItem rrsi = RATING_SCHEME_ITEM.as("rrsi");
        RatingSchemeItem crsi = RATING_SCHEME_ITEM.as("crsi");
        AssessmentRating ar = ASSESSMENT_RATING.as("ar");
        RatingScheme rrs = RATING_SCHEME.as("rrs");

        dsl.selectDistinct(ler.TARGET_ID, ler.TARGET_KIND, rrsi.ID)
                .from(ler)
                .innerJoin(ar).on(ar.ENTITY_ID.eq(ler.LEGAL_ENTITY_ID).and(ar.ENTITY_KIND.eq(EntityKind.LEGAL_ENTITY.name())))
                .innerJoin(crsi).on(ar.RATING_ID.eq(crsi.ID))
                .innerJoin(rrsi)
                    .on(rrsi.NAME.eq(crsi.RATING_GROUP))
                    .and(rrsi.SCHEME_ID.eq(DSL
                        .select(rrs.ID)
                        .from(rrs)
                        .where(rrs.EXTERNAL_ID.eq(LE_REGION_RS_EXT_ID))))
                .fetch()
                .stream()
                .map(r -> {
                    AssessmentRatingRecord rec = dsl.newRecord(ASSESSMENT_RATING);
                    rec.setRatingId(r.get(rrsi.ID));
                    rec.setEntityId(r.get(ler.TARGET_ID));
                    rec.setEntityKind(r.get(ler.TARGET_KIND));
                    rec.setProvenance(SAMPLE_DATA_PROVENANCE);
                    rec.setLastUpdatedBy(SAMPLE_DATA_USER);
                    rec.setIsReadonly(true);
                    rec.setAssessmentDefinitionId(regionAssessmentId);
                    return rec;
                })
                .collect(collectingAndThen(toSet(), dsl::batchStore))
                .execute();
    }

    private long createCountryAssessment(DSLContext dsl) {
        AssessmentDefinitionRecord r = dsl.newRecord(ASSESSMENT_DEFINITION);
        r.setName("Legal Entity Jurisdiction");
        r.setRatingSchemeId(dsl
                .select(RatingScheme.RATING_SCHEME.ID)
                .from(RatingScheme.RATING_SCHEME)
                .where(RatingScheme.RATING_SCHEME.EXTERNAL_ID.eq(COUNTRY_RS_EXT_ID))
                .fetchOne(RatingScheme.RATING_SCHEME.ID));
        r.setExternalId(LE_JURISDICTION_AD_EXT_ID);
        r.setProvenance(SAMPLE_DATA_PROVENANCE);
        r.setLastUpdatedBy(SAMPLE_DATA_USER);
        r.setCardinality(Cardinality.ZERO_ONE.name());
        r.setDefinitionGroup("Legal Entity");
        r.setEntityKind(EntityKind.LEGAL_ENTITY.name());

        r.store();

        return r.getId();
    }

    private Map<String, Long> createCountryRatingScheme(DSLContext dsl) {
        RatingSchemeRecord sr = dsl.newRecord(RatingScheme.RATING_SCHEME);
        sr.setName("Countries");
        sr.setExternalId(COUNTRY_RS_EXT_ID);
        sr.setDescription("List of countries, categorized by region");
        sr.store();

        countries.stream()
                .map(c -> {
                    RatingSchemeItemRecord cr = dsl.newRecord(RATING_SCHEME_ITEM);
                    cr.setName(c.name());
                    cr.setSchemeId(sr.getId());
                    cr.setRatingGroup(c.region());
                    cr.setExternalId(c.code());
                    cr.setCode(c.code());
                    cr.setDescription(format(
                            "Country: %s, Region: %s",
                            c.name(),
                            c.region()));
                    cr.setColor("green");
                    return cr;
                })
                .collect(collectingAndThen(toSet(), dsl::batchStore))
                .execute();

        return dsl
                .select(RATING_SCHEME_ITEM.CODE, RATING_SCHEME_ITEM.ID)
                .from(RATING_SCHEME_ITEM)
                .where(RATING_SCHEME_ITEM.SCHEME_ID.eq(sr.getId()))
                .fetchMap(RATING_SCHEME_ITEM.CODE, RATING_SCHEME_ITEM.ID);
    }


    private void createLegalEntityRelationships(DSLContext dsl, AtomicInteger ctr, long kindId) {
        List<Long> appIds = getAppIds(dsl);
        Set<Long> leIds = dsl
                .select(LEGAL_ENTITY.ID)
                .from(LEGAL_ENTITY)
                .fetchSet(LEGAL_ENTITY.ID);

        randomlySizedIntStream(100, 300)
                .mapToObj(i -> tuple(randomPick(appIds), randomPick(leIds)))
                .distinct()
                .map(t -> {
                    LegalEntityRelationshipRecord ler = dsl.newRecord(LEGAL_ENTITY_RELATIONSHIP);
                    ler.setLegalEntityId(t.v2);
                    ler.setRelationshipKindId(kindId);
                    ler.setTargetId(t.v1);
                    ler.setTargetKind(EntityKind.APPLICATION.name());
                    ler.setProvenance(SAMPLE_DATA_PROVENANCE);
                    ler.setLastUpdatedBy(SAMPLE_DATA_USER);
                    ler.setExternalId(format("LER_%d", ctr.getAndIncrement()));
                    return ler;
                })
                .collect(collectingAndThen(toSet(), dsl::batchStore))
                .execute();
    }

    /**
     * Returns the assessment Definition Id
     * @param dsl
     * @return assessment def id
     */
    private Long createRegionAssessment(DSLContext dsl) {
        long regionRatingSchemeId = dsl
                .insertInto(Tables.RATING_SCHEME)
                .set(Tables.RATING_SCHEME.NAME, "Legal Entity Region")
                .set(Tables.RATING_SCHEME.EXTERNAL_ID, LE_REGION_RS_EXT_ID)
                .set(Tables.RATING_SCHEME.DESCRIPTION, "Location of legal entity")
                .returning(Tables.RATING_SCHEME.ID)
                .fetchOne()
                .getId();

        log("Rating Scheme Id %d", regionRatingSchemeId);

        AssessmentDefinitionRecord appRegionAssessmentDef = dsl.newRecord(ASSESSMENT_DEFINITION);
        appRegionAssessmentDef.setName("Legal Entity Region");
        appRegionAssessmentDef.setDefinitionGroup("Regions associated to this app due to associated legal entities");
        appRegionAssessmentDef.setRatingSchemeId(regionRatingSchemeId);
        appRegionAssessmentDef.setEntityKind(EntityKind.APPLICATION.name());
        appRegionAssessmentDef.setDefinitionGroup("Legal Entity");
        appRegionAssessmentDef.setExternalId(LE_APP_REGION_AD_EXT_ID);
        appRegionAssessmentDef.setProvenance(SAMPLE_DATA_PROVENANCE);
        appRegionAssessmentDef.setLastUpdatedBy(SAMPLE_DATA_USER);
        appRegionAssessmentDef.setIsReadonly(true);
        appRegionAssessmentDef.setCardinality(Cardinality.ZERO_MANY.name());
        appRegionAssessmentDef.store();

        countries
                .stream()
                .map(c -> tuple(c.region(), c.regionCode()))
                .distinct()
                .map(t -> {
                    RatingSchemeItemRecord r = dsl.newRecord(RATING_SCHEME_ITEM);
                    r.setSchemeId(regionRatingSchemeId);
                    r.setName(t.v1);
                    r.setDescription(format("Region: %s (%s)", t.v1, t.v2));
                    r.setExternalId(t.v1.toUpperCase());
                    r.setCode(t.v2);
                    r.setColor("blue");
                    return r;
                })
                .collect(collectingAndThen(toSet(), dsl::batchStore))
                .execute();

        return appRegionAssessmentDef.getId();

    }

    private void createLegalEntityRecords(DSLContext dsl,
                                          long countryAssessmentId,
                                          Map<String, Long> countryCodeToRatingId,
                                          AtomicInteger ctr) {
        Set<Tuple3<Country, String, String>> les = randomPick(countries, 20)
                .stream()
                .flatMap(c -> randomlySizedIntStream(2, 40)
                        .mapToObj(x -> tuple(
                                c,
                                format("%s %s %s", randomPick(services), c.name(), randomPick(suffixes)),
                                format("LE_%d", ctr.getAndIncrement()))))
                .collect(toSet());

        les.stream()
                .map(t -> {
                    LegalEntityRecord ler = dsl.newRecord(LEGAL_ENTITY);
                    ler.setName(t.v2);
                    ler.setDescription(format("Description of %s", t.v2));
                    ler.setExternalId(t.v3);
                    ler.setProvenance(SAMPLE_DATA_PROVENANCE);
                    ler.setLastUpdatedBy(SAMPLE_DATA_USER);
                    ler.setEntityLifecycleStatus(EntityLifecycleStatus.ACTIVE.name());
                    return ler;
                })
                .collect(collectingAndThen(toSet(), dsl::batchStore))
                .execute();

        Map<String, Long> leExtIdToIdMap = dsl
                .select(LEGAL_ENTITY.EXTERNAL_ID, LEGAL_ENTITY.ID)
                .from(LEGAL_ENTITY)
                .fetchMap(LEGAL_ENTITY.EXTERNAL_ID, LEGAL_ENTITY.ID);

        les.stream()
                .map(t -> {
                    AssessmentRatingRecord r = dsl.newRecord(ASSESSMENT_RATING);
                    r.setAssessmentDefinitionId(countryAssessmentId);
                    r.setEntityKind(EntityKind.LEGAL_ENTITY.name());
                    r.setEntityId(leExtIdToIdMap.get(t.v3));
                    r.setRatingId(countryCodeToRatingId.get(t.v1.code()));
                    r.setLastUpdatedBy(SAMPLE_DATA_USER);
                    r.setProvenance(SAMPLE_DATA_PROVENANCE);
                    r.setDescription("Code: " +t.v1.code());
                    return r;
                })
                .collect(collectingAndThen(toSet(), dsl::batchStore))
                .execute();

    }


    private Set<Country> loadCountries() {
        return streamLines(getClass().getResourceAsStream("/regions.csv"))
                .skip(1)
                .map(line -> line.split(","))
                .map(cells -> ImmutableCountry
                        .builder()
                        .name(cells[0])
                        .code(cells[1])
                        .region(ArrayUtilities.idx(cells, 5, "?"))
                        .regionCode(ArrayUtilities.idx(cells, 7, "?"))
                        .build())
                .collect(toSet());
    }


    private static long mkRelKind(DSLContext dsl) {
        LegalEntityRelationshipKindRecord relKind = dsl.newRecord(LEGAL_ENTITY_RELATIONSHIP_KIND);
        relKind.setName("Controlling Legal Entity");
        relKind.setTargetKind(EntityKind.APPLICATION.name());
        relKind.setProvenance(SAMPLE_DATA_PROVENANCE);
        relKind.setCardinality(Cardinality.ZERO_ONE.name());
        relKind.setDescription("Controlled entity means a corporation, partnership, association, or other business entity with respect to which a bank possesses, directly or indirectly, the power to direct or cause the direction of management and policies, whether through the ownership of voting securities, by contract, or otherwise.");
        relKind.setExternalId("CLE");
        relKind.setLastUpdatedBy(SAMPLE_DATA_USER);
        relKind.store();
        return relKind.getId();
    }


    @Override
    public boolean remove(ApplicationContext ctx) {

        getDsl(ctx)
                .deleteFrom(ASSESSMENT_DEFINITION)
                .where(ASSESSMENT_DEFINITION.EXTERNAL_ID.in(LE_REGION_AD_EXT_ID, LE_JURISDICTION_AD_EXT_ID, LE_APP_REGION_AD_EXT_ID))
                .execute();
        getDsl(ctx)
                .deleteFrom(RATING_SCHEME_ITEM)
                .where(RATING_SCHEME_ITEM.SCHEME_ID.in(DSL
                        .select(RATING_SCHEME.ID)
                        .from(RATING_SCHEME)
                        .where(RATING_SCHEME.EXTERNAL_ID.in(LE_REGION_RS_EXT_ID, COUNTRY_RS_EXT_ID))))
                .execute();
        getDsl(ctx)
                .deleteFrom(RATING_SCHEME)
                .where(RATING_SCHEME.EXTERNAL_ID.in(LE_REGION_RS_EXT_ID, COUNTRY_RS_EXT_ID))
                .execute();
        getDsl(ctx)
                .deleteFrom(LEGAL_ENTITY_RELATIONSHIP)
                .execute();
        getDsl(ctx)
                .deleteFrom(LEGAL_ENTITY)
                .execute();
        getDsl(ctx)
                .deleteFrom(LEGAL_ENTITY_RELATIONSHIP_KIND)
                .execute();
        return true;
    }

}
