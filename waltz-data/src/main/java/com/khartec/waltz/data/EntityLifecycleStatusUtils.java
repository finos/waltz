package com.khartec.waltz.data;


import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.model.EntityLifecycleStatus;

import java.util.List;
import java.util.Set;

public class EntityLifecycleStatusUtils {

    public static List<Boolean> convertToIsRemovedFlags(Set<EntityLifecycleStatus> entityLifecycleStatuses) {
        boolean includeRemoved = entityLifecycleStatuses.contains(EntityLifecycleStatus.REMOVED);
        boolean includedNonRemoved = !(entityLifecycleStatuses.contains(EntityLifecycleStatus.ACTIVE)
                || entityLifecycleStatuses.contains(EntityLifecycleStatus.PENDING));

        return ListUtilities.newArrayList(includedNonRemoved, includeRemoved);
    }

}
