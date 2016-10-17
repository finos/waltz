package com.khartec.waltz.model.command;

import com.khartec.waltz.model.LastUpdate;

import java.util.Optional;


public interface EntityChangeCommand {

    long id();
    Optional<LastUpdate> lastUpdate();

}
