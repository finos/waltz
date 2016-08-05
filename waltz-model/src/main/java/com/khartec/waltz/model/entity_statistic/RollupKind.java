package com.khartec.waltz.model.entity_statistic;

/**
 * Created by dwatkins on 05/08/2016.
 */
public enum RollupKind {

    /**
     * This means group by outcome, then count distinct on entity references
     */
    COUNT_BY_ENTITY,

    /**
     * This means group by outcome, then sum the values in each group (entity references are discarded)
     */
    SUM_BY_VALUE
}
