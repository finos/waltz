package com.khartec.waltz.integration_test.inmem.helpers;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.involvement.EntityInvolvementChangeCommand;
import com.khartec.waltz.model.involvement.ImmutableEntityInvolvementChangeCommand;
import com.khartec.waltz.model.involvement_kind.ImmutableInvolvementKindCreateCommand;
import com.khartec.waltz.model.involvement_kind.InvolvementKindCreateCommand;
import com.khartec.waltz.service.involvement.InvolvementService;
import com.khartec.waltz.service.involvement_kind.InvolvementKindService;

import static com.khartec.waltz.integration_test.inmem.helpers.NameHelper.mkUserId;
import static com.khartec.waltz.model.EntityReference.mkRef;

public class InvolvementHelper {

    private final InvolvementService involvementService;
    private final InvolvementKindService involvementKindService;

    public InvolvementHelper(InvolvementService involvementService,
                             InvolvementKindService involvementKindService) {
        this.involvementService = involvementService;
        this.involvementKindService = involvementKindService;
    }


    public long mkInvolvement(String name) {
        InvolvementKindCreateCommand cmd = ImmutableInvolvementKindCreateCommand.builder()
                .description(name)
                .name(name)
                .build();
        return involvementKindService.create(cmd, mkUserId("involvementHelper"));
    }


    public void createInvolvement(Long pId, long invId, EntityReference entity) {
        EntityInvolvementChangeCommand cmd = ImmutableEntityInvolvementChangeCommand.builder()
                .involvementKindId((int) invId)
                .personEntityRef(mkRef(EntityKind.PERSON, pId))
                .operation(Operation.ADD)
                .build();
        involvementService.addEntityInvolvement(mkUserId(), entity, cmd);
    }
}
