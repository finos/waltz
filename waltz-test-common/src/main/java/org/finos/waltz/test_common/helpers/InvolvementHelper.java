package org.finos.waltz.test_common.helpers;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.command.ImmutableFieldChange;
import org.finos.waltz.model.involvement.EntityInvolvementChangeCommand;
import org.finos.waltz.model.involvement.ImmutableEntityInvolvementChangeCommand;
import org.finos.waltz.model.involvement_kind.ImmutableInvolvementKindChangeCommand;
import org.finos.waltz.model.involvement_kind.ImmutableInvolvementKindCreateCommand;
import org.finos.waltz.model.involvement_kind.InvolvementKindCreateCommand;
import org.finos.waltz.service.involvement.InvolvementService;
import org.finos.waltz.service.involvement_kind.InvolvementKindService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.model.EntityReference.mkRef;

@Service
public class InvolvementHelper {

    private final InvolvementService involvementService;
    private final InvolvementKindService involvementKindService;

    @Autowired
    public InvolvementHelper(InvolvementService involvementService,
                             InvolvementKindService involvementKindService) {
        this.involvementService = involvementService;
        this.involvementKindService = involvementKindService;
    }


    public long mkInvolvementKind(String name) {
        InvolvementKindCreateCommand cmd = ImmutableInvolvementKindCreateCommand.builder()
                .description(name)
                .name(name)
                .externalId(name)
                .subjectKind(EntityKind.APPLICATION)
                .build();
        return involvementKindService.create(cmd, NameHelper.mkUserId("involvementHelper"));
    }


    public void createInvolvement(Long pId, long invId, EntityReference entity) {
        EntityInvolvementChangeCommand cmd = ImmutableEntityInvolvementChangeCommand.builder()
                .involvementKindId((int) invId)
                .personEntityRef(mkRef(EntityKind.PERSON, pId))
                .operation(Operation.ADD)
                .build();
        involvementService.addEntityInvolvement(NameHelper.mkUserId(), entity, cmd);
    }


    public void markAsIntransitive(long kindId) {
        involvementKindService.update(
                ImmutableInvolvementKindChangeCommand
                        .builder()
                        .id(kindId)
                        .transitive(ImmutableFieldChange.<Boolean>builder()
                                .oldVal(true)
                                .newVal(false)
                                .build())
                        .build(),
                "admin");
    }
}
