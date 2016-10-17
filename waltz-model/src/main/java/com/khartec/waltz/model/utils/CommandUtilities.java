package com.khartec.waltz.model.utils;

import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.model.ImmutableLastUpdate;
import com.khartec.waltz.model.LastUpdate;
import com.khartec.waltz.model.command.ChangeFieldCommand;
import com.khartec.waltz.model.command.ImmutableChangeFieldCommand;

import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;

public class CommandUtilities {

    public static ChangeFieldCommand withUser(ChangeFieldCommand cmd, String username) {
        checkNotNull(cmd, "cmd cannot be null");
        checkNotNull(username, "username cannot be null");

        LastUpdate lastUpdate = ImmutableLastUpdate.builder()
                .by(username)
                .build();

        return ImmutableChangeFieldCommand
                .copyOf(cmd)
                .withLastUpdate(lastUpdate);
    }


    public static Optional<Integer> newValAsInt(ChangeFieldCommand cmd) {
        checkNotNull(cmd, "cmd cannot be null");
        if (StringUtilities.isEmpty(cmd.newVal())) {
            return Optional.empty();
        } else {
            return Optional.of(Integer.parseInt(cmd.newVal()));
        }
    }
}
