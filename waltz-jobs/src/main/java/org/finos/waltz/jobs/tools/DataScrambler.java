package org.finos.waltz.jobs.tools;

import io.codearte.jfairy.Fairy;
import io.codearte.jfairy.producer.person.Person;
import org.finos.waltz.common.IOUtilities;
import org.finos.waltz.schema.tables.records.ApplicationRecord;
import org.finos.waltz.schema.tables.records.ChangeInitiativeRecord;
import org.finos.waltz.schema.tables.records.CostRecord;
import org.finos.waltz.schema.tables.records.PersonRecord;
import org.finos.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.RandomUtilities.randomPick;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.schema.Tables.*;

public class DataScrambler {

    private static final Fairy fairy = Fairy.create();

    public static void main(String[] args) throws IOException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        dsl.transaction(context -> {

            DSLContext tx = context.dsl();

            scrambleApplicationNames(tx);
            scramblePersonNames(tx);
            scrambleCosts(tx);
            scrambleChangeInitiativeNames(tx);

//            throw new IllegalArgumentException("BbbooooOOOOOOoommmmMM!!!");

        });
    }


    private static void scrambleChangeInitiativeNames(DSLContext tx) {

        Set<String> namePt1 = asSet(
                "Change", "Enhance", "Deliver", "Adapt to", "Meet",
                "Invest in", "Perform", "Undertake", "Manage",
                "Analyze", "Restructure", "Lead", "Prioritise",
                "Reduce", "Lower");

        Set<String> namePt2 = asSet(
                "Regulatory", "Compliance", "Market",
                "Global", "Regional", "Tactical", "Enterprise",
                "Industry", "Governance", "Auditor",
                "Business", "Customer");

        Set<String> namePt3 = asSet(
                "Processes", "Standards", "Trends",
                "Initiatives", "Reporting", "Operations", "Aggregation",
                "Structures");

        Set<ChangeInitiativeRecord> changeInitiativeRecords = tx
                .select()
                .from(CHANGE_INITIATIVE)
                .fetchSet(r -> r.into(CHANGE_INITIATIVE));

        int[] updates = changeInitiativeRecords
                .stream()
                .map(r -> {
                    String name = format("%s %s %s", randomPick(namePt1), randomPick(namePt2), randomPick(namePt3));
                    r.setName(name);
                    return r;
                })
                .collect(collectingAndThen(toSet(), d -> tx.batchUpdate(d).execute()));

        System.out.println(format("Updated %d change initiative records with a random name", IntStream.of(updates).sum()));

    }


    private static void scrambleCosts(DSLContext tx) {

        Set<CostRecord> costRecords = tx
                .select()
                .from(COST)
                .fetchSet(r -> r.into(COST));

        int[] updates = costRecords
                .stream()
                .map(r -> {

                    BigDecimal max = new BigDecimal(10000);
                    BigDecimal randFromDouble = new BigDecimal(Math.random());
                    BigDecimal actualRandomDec = randFromDouble.multiply(max);
                    BigDecimal newCost = actualRandomDec.setScale(2, RoundingMode.HALF_UP);

                    r.setAmount(newCost);

                    return r;
                })
                .collect(collectingAndThen(toSet(), d -> tx.batchUpdate(d).execute()));

        System.out.println(format("Updated %d cost records with a random value", IntStream.of(updates).sum()));

    }


    private static void scramblePersonNames(DSLContext tx) {

        Set<PersonRecord> personRecords = tx
                .select()
                .from(PERSON)
                .fetchSet(r -> r.into(PERSON));

        int[] updates = personRecords
                .stream()
                .map(r -> {
                    Person person = fairy.person();

                    r.setDisplayName(person.getFullName());
                    r.setMobilePhone(person.getTelephoneNumber());

                    return r;
                })
                .collect(collectingAndThen(toSet(), d -> tx.batchUpdate(d).execute()));

        System.out.println(format("Updated %d person records with a random display name and number", IntStream.of(updates).sum()));

    }


    private static void scrambleApplicationNames(DSLContext tx) {

        Set<String> animalApps = IOUtilities
                .streamLines(DataScrambler.class.getClassLoader().getResourceAsStream("app-names.txt"))
                .collect(toSet());

        Set<ApplicationRecord> appRecords = tx
                .select()
                .from(APPLICATION)
                .fetchSet(r -> r.into(APPLICATION));

        int[] updates = appRecords
                .stream()
                .map(r -> {
                    String appName = randomPick(animalApps);
                    r.setName(appName);
                    return r;
                })
                .collect(collectingAndThen(toSet(), d -> tx.batchUpdate(d).execute()));

        System.out.println(format("Updated %d application records with a random name", IntStream.of(updates).sum()));

    }
}
