package org.finos.waltz.test_common.playwright;

/**
 * List of the waltz ui sections and their id's.
 */
public enum Section {
    ASSESSMENTS(200);


    private int id;

    Section(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }
}
