package com.khartec.waltz.jobs.tools;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.common.StreamUtilities.Siphon;
import com.khartec.waltz.jobs.tools.resolvers.InvolvementNameToIdResolver;
import com.khartec.waltz.jobs.tools.resolvers.OrgNameToIdResolver;
import com.khartec.waltz.jobs.tools.resolvers.PersonNameToEmpIdResolver;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.schema.tables.records.InvolvementRecord;
import com.khartec.waltz.service.DIBaseConfiguration;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.khartec.waltz.common.DebugUtilities.dump;
import static com.khartec.waltz.common.SetUtilities.map;
import static com.khartec.waltz.common.StreamUtilities.mkSiphon;
import static java.lang.String.format;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class PersonResolver {



    private static final List<Tuple2<String, String>> data = ListUtilities.newArrayList(
        tuple("Example Org A", "David Watkins"),
        tuple("Example Org B", "Fred Bloggs")
    );

    private static String involvementKindName = "Domain Architect";
    private static String provenance = "DOMAIN_ARCH_IMPORT";

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIBaseConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        InvolvementNameToIdResolver involvementNameToIdResolver = new InvolvementNameToIdResolver(dsl);
        PersonNameToEmpIdResolver personNameToEmpIdResolver = new PersonNameToEmpIdResolver(dsl);
        OrgNameToIdResolver orgNameToIdResolver = new OrgNameToIdResolver(dsl);

        Siphon<Tuple4<String, String, Optional<Long>, Optional<String>>> noOrgSiphon = mkSiphon(t -> !t.v3.isPresent());
        Siphon<Tuple4<String, String, Optional<Long>, Optional<String>>> noPersonSiphon = mkSiphon(t -> !t.v4.isPresent());


        Set<Tuple3<Long, String, Long>> orgEmpInvTuples = involvementNameToIdResolver
                .resolve(involvementKindName)
                .map(involvementKindId -> data
                        .stream()
                        .flatMap(t -> Stream
                                .of(t.v2.split(" / "))
                                .map(name -> tuple(t.v1, name)))
                        .distinct()
                        .map(t -> t.concat(orgNameToIdResolver.resolve(t.v1)))
                        .map(t -> t.concat(personNameToEmpIdResolver.resolve(t.v2)))
                        .filter(noOrgSiphon)
                        .filter(noPersonSiphon)
                        .map(t -> t
                                .skip2() // throw away raw-data
                                .map1(Optional::get) // empId
                                .map2(Optional::get) // orgId
                                .concat(involvementKindId))
                        .collect(Collectors.toSet()))
                .orElseThrow(() -> new IllegalArgumentException(format("Cannot find involvement kind: %s", involvementKindName)));

        dump("No Org", noOrgSiphon, t -> t.v1);
        dump("No Person", noPersonSiphon, t -> t.v2);

        Set<InvolvementRecord> records = map(orgEmpInvTuples, t -> new InvolvementRecord(EntityKind.ORG_UNIT.name(), t.v1, t.v2, provenance, t.v3));
        dsl.batchInsert(records).execute();

    }


}
