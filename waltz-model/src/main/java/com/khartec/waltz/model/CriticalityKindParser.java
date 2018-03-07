package com.khartec.waltz.model;

import com.khartec.waltz.common.Aliases;
import com.khartec.waltz.common.EnumParser;

public class CriticalityKindParser extends EnumParser<Criticality> {

    private static final Aliases<Criticality> defaultAliases = new Aliases<>();


    public CriticalityKindParser() {
        super(defaultAliases, Criticality.class);
    }
}