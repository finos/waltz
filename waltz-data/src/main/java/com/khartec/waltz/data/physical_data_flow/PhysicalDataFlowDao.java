package com.khartec.waltz.data.physical_data_flow;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.khartec.waltz.common.Checks.checkNotNull;

/**
 * Created by dwatkins on 03/10/2016.
 */
@Repository
public class PhysicalDataFlowDao {

    private final DSLContext dsl;


    @Autowired
    public PhysicalDataFlowDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }

    public void foo() {


    }
}
