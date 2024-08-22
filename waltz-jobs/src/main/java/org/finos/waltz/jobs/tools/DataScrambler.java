package org.finos.waltz.jobs.tools;

import io.codearte.jfairy.Fairy;
import io.codearte.jfairy.producer.person.Person;
import org.finos.waltz.common.IOUtilities;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.schema.tables.records.ApplicationRecord;
import org.finos.waltz.schema.tables.records.ChangeInitiativeRecord;
import org.finos.waltz.schema.tables.records.CostRecord;
import org.finos.waltz.schema.tables.records.PersonRecord;
import org.finos.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.RandomUtilities.randomPick;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.data.JooqUtilities.summarizeResults;
import static org.finos.waltz.schema.Tables.APPLICATION;
import static org.finos.waltz.schema.Tables.CHANGE_INITIATIVE;
import static org.finos.waltz.schema.Tables.COST;
import static org.finos.waltz.schema.Tables.ORGANISATIONAL_UNIT;
import static org.finos.waltz.schema.Tables.PERSON;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class DataScrambler {

    private static final Fairy fairy = Fairy.create();

    public static void main(String[] args) throws IOException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        dsl.transaction(context -> {

            DSLContext tx = context.dsl();

//            scrambleApplicationNames(tx);
            scrambleApplicationNames2(tx);
            scramblePersonNames(tx);
            scrambleCosts(tx);
            scrambleChangeInitiativeNames(tx);
            scrambleOrgUnitNames(tx);

//            throw new IllegalArgumentException("BbbooooOOOOOOoommmmMM!!!");

        });
    }

    private static void scrambleOrgUnitNames(DSLContext tx) {

        Set<String> animalApps = IOUtilities
                .streamLines(DataScrambler.class.getClassLoader().getResourceAsStream("app-names.txt"))
                .collect(toSet());

        int updateCount = summarizeResults(tx
                .selectFrom(ORGANISATIONAL_UNIT)
                .fetch()
                .stream()
                .map(r -> {
                    String origName = r.getName();
                    String newName = updateName(
                            origName,
                            asSet(
                                    tuple("db", asSet("MyCorp")),
                                    tuple("pb", asSet("Private Banking")),
                                    tuple("postbank", asSet("MySubCorp")),
                                    tuple("norisbank", asSet("MyMiniCorp2")),
                                    tuple("wm ", asSet("HNW Clients")),
                                    tuple("total divisions & infrastructure", asSet("Technology")),
                                    tuple("tdi", asSet("Tech")),
                                    tuple("DWS", asSet("MyDept")),
                                    tuple("cb ib", asSet("Corporate and Investment IT")),
                                    tuple("globe", asSet("Planet", "World", "WholeEarth", "Global")),
                                    tuple("ib", asSet("Investment Division")),
                                    tuple("cb", asSet("Corporate Division"))
                            ));
                    if (newName.equals(origName)) {
                        return null;
                    } else {
                        r.setName(newName);
                        return r;
                    }
                })
                .filter(Objects::nonNull)
                .collect(collectingAndThen(
                        toSet(),
                        tx::batchStore))
                .execute());

        System.out.println("Updated: " + updateCount);
    }

    private static void scrambleApplicationNames2(DSLContext tx) {

        Set<String> animalApps = IOUtilities
                .streamLines(DataScrambler.class.getClassLoader().getResourceAsStream("app-names.txt"))
                .collect(toSet());

        int updateCount = summarizeResults(tx
                .selectFrom(APPLICATION)
                .where(APPLICATION.ENTITY_LIFECYCLE_STATUS.notEqual(EntityLifecycleStatus.REMOVED.name()))
                .fetch()
                .stream()
                .map(r -> {
                    String origName = r.getName();
                    String newName = updateName(
                            origName,
                            asSet(
                                    tuple("DWS", asSet("MyDept")),
                                    tuple("Smaragd", asSet(randomPick(animalApps))),
                                    tuple("aurora", asSet(randomPick(animalApps))),
                                    tuple("autobahn", asSet(randomPick(animalApps))),
                                    tuple("b@fir", asSet(randomPick(animalApps))),
                                    tuple("bhw", asSet(randomPick(animalApps))),
                                    tuple("bms", asSet(randomPick(animalApps))),
                                    tuple("bnym", asSet(randomPick(animalApps))),
                                    tuple("brds", asSet(randomPick(animalApps))),
                                    tuple("brs", asSet(randomPick(animalApps))),
                                    tuple("combat", asSet(randomPick(animalApps))),
                                    tuple("cosmos", asSet(randomPick(animalApps))),
                                    tuple("crest", asSet(randomPick(animalApps))),
                                    tuple("cres", asSet(randomPick(animalApps))),
                                    tuple("cro", asSet(randomPick(animalApps))),
                                    tuple("dashboard", asSet("Portal", "Overview", "Dashboard")),
                                    tuple("data", asSet("Info", "Dataset", "Infobase", "Data")),
                                    tuple("db", asSet("MyCorp")),
                                    tuple("directory", asSet("Listing", "Pages", "YellowPages")),
                                    tuple("eagle", asSet(randomPick(animalApps))),
                                    tuple("fdw", asSet(randomPick(animalApps))),
                                    tuple("global", asSet("Planet", "World", "WholeEarth", "Globe")),
                                    tuple("globe", asSet("Planet", "World", "WholeEarth", "Global")),
                                    tuple("gm", asSet("XY", "AB")),
                                    tuple("gpc", asSet(randomPick(animalApps))),
                                    tuple("internet", asSet("Net", "Web", "INet", "WWW")),
                                    tuple("kannon", asSet(randomPick(animalApps))),
                                    tuple("kofax", asSet(randomPick(animalApps))),
                                    tuple("kondor", asSet(randomPick(animalApps))),
                                    tuple("krx", asSet(randomPick(animalApps))),
                                    tuple("ksfc", asSet(randomPick(animalApps))),
                                    tuple("ksg", asSet(randomPick(animalApps))),
                                    tuple("mantas", asSet(randomPick(animalApps))),
                                    tuple("norisbank", asSet("MyMiniCorp2")),
                                    tuple("palace", asSet(randomPick(animalApps))),
                                    tuple("pb", asSet("MySubCorp")),
                                    tuple("phoenix", asSet(randomPick(animalApps))),
                                    tuple("postbank", asSet("MyMiniCorp")),
                                    tuple("pwcc", asSet("MyMiniCorp2")),
                                    tuple("pwm", asSet("MySubCorp")),
                                    tuple("pws", asSet(randomPick(animalApps))),
                                    tuple("quality", asSet("Quality", "Assurance", "Verifier")),
                                    tuple("quantum", asSet("Solar", "Time", "Solace", "Atom", "Tunnel")),
                                    tuple("quasar", asSet(randomPick(animalApps))),
                                    tuple("quest", asSet(randomPick(animalApps))),
                                    tuple("racer", asSet(randomPick(animalApps))),
                                    tuple("race", asSet(randomPick(animalApps))),
                                    tuple("saturn", asSet(randomPick(animalApps))),
                                    tuple("signet", asSet(randomPick(animalApps))),
                                    tuple("sparta", asSet(randomPick(animalApps))),
                                    tuple("spider", asSet(randomPick(animalApps))),
                                    tuple("tas ", asSet("MyMiniCorp3")),
                                    tuple("tm3", asSet(randomPick(animalApps))),
                                    tuple("traiana", asSet(randomPick(animalApps))),
                                    tuple("trappist", asSet(randomPick(animalApps))),
                                    tuple("trinity", asSet(randomPick(animalApps))),
                                    tuple("voyager", asSet("Traveller", "Explorer", "Backpacker")),
                                    tuple("wm ", asSet("MyMiniCorp4"))
                            ));
                    if (newName.equals(origName)) {
                        return null;
                    } else {
                        r.setName(newName);
                        return r;
                    }
                })
                .filter(Objects::nonNull)
                .collect(collectingAndThen(
                        toSet(),
                        tx::batchStore))
                .execute());

        System.out.println("Updated: " + updateCount);
    }


    private static String updateName(String name, Set<Tuple2<String, Set<String>>> replacers) {
        String working = name;
        for (Tuple2<String, Set<String>> t : replacers) {
            working = working.replaceAll("(?i)"+t.v1+"(?-i)", randomPick(t.v2));
        }
        return working;
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
                "Business", "Customer", "Efficiency", "Benefits");

        Set<String> namePt3 = asSet(
                "Processes", "Standards", "Trends",
                "Initiatives", "Reporting", "Operations", "Aggregation",
                "Structures", "Automation", "Investigation", "Remediation");

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
