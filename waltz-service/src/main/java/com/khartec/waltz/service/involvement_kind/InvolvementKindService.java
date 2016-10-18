package com.khartec.waltz.service.involvement_kind;

import com.khartec.waltz.data.involvement_kind.InvolvementKindDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.LastUpdate;
import com.khartec.waltz.model.command.CommandOutcome;
import com.khartec.waltz.model.command.CommandResponse;
import com.khartec.waltz.model.command.ImmutableCommandResponse;
import com.khartec.waltz.model.invovement_kind.ImmutableInvolvementKindChangeCommand;
import com.khartec.waltz.model.invovement_kind.InvolvementKind;
import com.khartec.waltz.model.invovement_kind.InvolvementKindChangeCommand;
import com.khartec.waltz.model.invovement_kind.InvolvementKindCreateCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class InvolvementKindService {

    private final InvolvementKindDao involvementKindDao;


    @Autowired
    public InvolvementKindService(InvolvementKindDao involvementKindDao) {
        checkNotNull(involvementKindDao, "involvementKindDao cannot be null");

        this.involvementKindDao = involvementKindDao;
    }


    public List<InvolvementKind> findAll() {
        return involvementKindDao.findAll();
    }


    public InvolvementKind getById(long id) {
        return involvementKindDao.getById(id);
    }


    public Long create(InvolvementKindCreateCommand command, String username) {
        checkNotNull(command, "command cannot be null");
        checkNotNull(username, "username cannot be null");

        return involvementKindDao.create(command, username);
    }


    public CommandResponse<InvolvementKindChangeCommand> update(InvolvementKindChangeCommand command, String username) {
        checkNotNull(command, "command cannot be null");
        checkNotNull(username, "username cannot be null");

        ImmutableInvolvementKindChangeCommand updateCommand = ImmutableInvolvementKindChangeCommand
                .copyOf(command)
                .withLastUpdate(LastUpdate.mkForUser(username));

        boolean success = involvementKindDao.update(updateCommand);
        return ImmutableCommandResponse.<InvolvementKindChangeCommand>builder()
                .originalCommand(command)
                .entityReference(EntityReference.mkRef(EntityKind.INVOLVEMENT_KIND, command.id()))
                .outcome(success ? CommandOutcome.SUCCESS : CommandOutcome.FAILURE)
                .build();
    }


    public boolean delete(long id) {
        return involvementKindDao.deleteIfNotUsed(id);
    }

}
