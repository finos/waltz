package com.khartec.waltz.model.utils;

import com.khartec.waltz.model.ImmutableLastUpdate;
import com.khartec.waltz.model.LastUpdate;

import java.time.LocalDateTime;

public class CommandUtilities {

    public static LastUpdate mkLastUpdate(String username, LocalDateTime time) {
        return ImmutableLastUpdate.builder()
                .by(username)
                .at(time)
                .build();
    }

}
