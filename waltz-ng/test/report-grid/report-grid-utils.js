describe("report-grid/report-grid-utils", () => {
    describe("determineDefaultRollupRule", () => {
        it("determine best rollup rule for a given column entity", () => {
            const cost = {id: 1, kind: "COST"};
            const assessment = {id: 1, kind: "ASSESSMENT_DEFINITION"};
            const involvement = {id: 1, kind: "INVOLVEMENT_KIND"};
            const leafMeasurable = {id: 1, kind: "MEASURABLE", concrete: true};
            const branchMeasurable = {id: 1, kind: "MEASURABLE", concrete: false};

            // assert.equal(ratingRollupRule.NONE, determineDefaultRollupRule(cost));
            // assert.equal(ratingRollupRule.NONE, determineDefaultRollupRule(assessment));
            // assert.equal(ratingRollupRule.NONE, determineDefaultRollupRule(involvement));
            // assert.equal(ratingRollupRule.PICK_HIGHEST, determineDefaultRollupRule(leafMeasurable));
            // assert.equal(ratingRollupRule.PICK_HIGHEST, determineDefaultRollupRule(branchMeasurable));
        });
    });
});