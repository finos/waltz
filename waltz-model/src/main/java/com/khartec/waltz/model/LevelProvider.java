package com.khartec.waltz.model;

/**
 * Created by dwatkins on 20/07/2016.
 */
public interface LevelProvider {

    int level();

    @Nullable
    Long level1();

    @Nullable
    Long level2();

    @Nullable
    Long level3();

    @Nullable
    Long level4();

    @Nullable
    Long level5();

}
