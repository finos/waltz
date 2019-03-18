package com.khartec.waltz.jobs.tools;

import com.khartec.waltz.common.IOUtilities;
import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.user.UserRoleService;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;

public class BulkRoleAssign {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        UserRoleService userRoleService = ctx.getBean(UserRoleService.class);

        Set<Role> defaultRoles = SetUtilities.asSet(
                Role.BOOKMARK_EDITOR,
                Role.LOGICAL_DATA_FLOW_EDITOR,
                Role.LINEAGE_EDITOR);

        Set<Role> mustHaveRoles = SetUtilities.asSet(
                Role.TAXONOMY_EDITOR,
                Role.CAPABILITY_EDITOR,
                Role.RATING_EDITOR);

        InputStream inputStream = BulkRoleAssign.class.getClassLoader().getResourceAsStream("bulk-role-assign-example.txt");
        Set<Tuple2<String, Set<Role>>> updates = IOUtilities
                .streamLines(inputStream)
                .map(d -> d.toLowerCase().trim())
                .map(d -> Tuple.tuple(d, userRoleService.getUserRoles(d)))
                .map(t -> t.map2(existingRoles -> SetUtilities.union(existingRoles, defaultRoles, mustHaveRoles)))
                .collect(Collectors.toSet());

        System.out.printf("About to update: %d user-role mappings\n", updates.size());
        updates.forEach(t -> userRoleService.updateRoles("admin", t.v1, t.v2));
        System.out.println("Finished updating mappings");
    }
}
