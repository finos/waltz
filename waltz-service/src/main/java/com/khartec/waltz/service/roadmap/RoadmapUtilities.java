package com.khartec.waltz.service.roadmap;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;

public class RoadmapUtilities {

    public static ChangeLog mkBasicLogEntry(long scenarioId, String message, String userId) {
        return ImmutableChangeLog.builder()
                .message(message)
                .parentReference(EntityReference.mkRef(EntityKind.ROADMAP, scenarioId))
                .operation(Operation.UPDATE)
                .userId(userId)
                .severity(Severity.INFORMATION)
                .build();
    }

}
