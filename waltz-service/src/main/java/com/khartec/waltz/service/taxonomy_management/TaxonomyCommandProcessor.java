package com.khartec.waltz.service.taxonomy_management;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.taxonomy_management.TaxonomyChangeCommand;
import com.khartec.waltz.model.taxonomy_management.TaxonomyChangePreview;
import com.khartec.waltz.model.taxonomy_management.TaxonomyChangeType;

public interface TaxonomyCommandProcessor {

    default void checkType(TaxonomyChangeCommand cmd, TaxonomyChangeType expectedType) {
        Checks.checkTrue(
                cmd.changeType() == expectedType,
                "Incorrect type, expected [$s] got [%s]",
                expectedType,
                cmd.changeType());
    }

    default void checkDomain(TaxonomyChangeCommand cmd, EntityKind expectedDomain) {
        Checks.checkTrue(
                cmd.changeDomain().kind() == expectedDomain,
                "Incorrect domain, expected [$s] got [%s]",
                expectedDomain,
                cmd.changeDomain().kind());
    }

    TaxonomyChangePreview preview(TaxonomyChangeCommand cmd);
    TaxonomyChangeCommand apply(TaxonomyChangeCommand command, String userId);

    TaxonomyChangeType type();

}
