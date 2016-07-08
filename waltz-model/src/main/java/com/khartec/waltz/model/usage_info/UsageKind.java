package com.khartec.waltz.model.usage_info;

public enum UsageKind {
    CONSUMER(true),
    DISTRIBUTOR(true),
    MODIFIER(false),
    ORIGINATOR(false);


    private final boolean readOnly;

    UsageKind(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

}
