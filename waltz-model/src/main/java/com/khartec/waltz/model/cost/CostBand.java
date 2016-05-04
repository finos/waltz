package com.khartec.waltz.model.cost;

import com.khartec.waltz.common.RangeBand;

import java.math.BigDecimal;

public class CostBand extends RangeBand<BigDecimal> {

    public CostBand(BigDecimal low, BigDecimal high) {
        super(low, high);
    }

}
