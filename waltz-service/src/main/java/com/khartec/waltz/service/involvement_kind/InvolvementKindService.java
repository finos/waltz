package com.khartec.waltz.service.involvement_kind;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.data.involvement_kind.InvolvementKindDao;
import com.khartec.waltz.model.invovement_kind.ImmutableInvolvementKindChangeCommand;
import com.khartec.waltz.model.invovement_kind.InvolvementKind;
import com.khartec.waltz.model.invovement_kind.InvolvementKindChangeCommand;
import com.khartec.waltz.model.invovement_kind.InvolvementKindCreateCommand;
import com.khartec.waltz.model.utils.CommandUtilities;
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


    public Long update(InvolvementKindChangeCommand command, String username) {
        checkNotNull(command, "command cannot be null");
        checkNotNull(username, "username cannot be null");

        ImmutableInvolvementKindChangeCommand updateCommand = ImmutableInvolvementKindChangeCommand
                .copyOf(command)
                .withLastUpdate(CommandUtilities.mkLastUpdate(username, DateTimeUtilities.nowUtc()));

        return involvementKindDao.update(updateCommand);
    }


    public boolean delete(long id) {
        return involvementKindDao.deleteIfNotUsed(id);
    }

}
