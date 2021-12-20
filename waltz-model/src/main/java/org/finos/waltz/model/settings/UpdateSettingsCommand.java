package org.finos.waltz.model.settings;

import org.finos.waltz.model.command.Command;

public abstract class UpdateSettingsCommand implements Command {

    public abstract String name();
    public abstract String value();

}
