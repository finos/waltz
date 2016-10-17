package com.khartec.waltz.model.utils;

import com.khartec.waltz.model.ImmutableLastUpdate;
import com.khartec.waltz.model.LastUpdate;
import com.khartec.waltz.model.command.EntityChangeCommand;
import com.khartec.waltz.model.command.ImmutableEntityChangeCommand;

import static com.khartec.waltz.common.Checks.checkNotNull;

public class CommandUtilities {

    public static EntityChangeCommand withUser(EntityChangeCommand cmd, String username) {
        checkNotNull(cmd, "cmd cannot be null");
        checkNotNull(username, "username cannot be null");

        LastUpdate lastUpdate = ImmutableLastUpdate.builder()
                .by(username)
                .build();

        return ImmutableEntityChangeCommand
                .copyOf(cmd)
                .withLastUpdate(lastUpdate);
    }

}
