package com.khartec.waltz.jobs.clients.c1.sc1;

import com.khartec.waltz.jobs.SheetNumProvider;

public enum SheetDefinition implements SheetNumProvider {

    APPLICATION(0),
    COMPONENT(1),
    FLOWS(2),
    DOMAIN(3),
    PROJECT(4),
    BUSINESS_SUPPORT(5);

    private final int sheetNum;

    SheetDefinition(int sheetNum) {
        this.sheetNum = sheetNum;
    }

    public int sheetNum() {
        return sheetNum;
    }
}
