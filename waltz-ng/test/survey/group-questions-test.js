import {assert} from "chai";
import {groupQuestions} from "../../client/survey/survey-utils";

describe("survey/survey-utils", () => {
    describe("groupQuestions", () => {
        it("gives empty list if no questions given", () => {
            assert.isEmpty(groupQuestions([]));
            assert.isEmpty(groupQuestions());
            assert.isEmpty(groupQuestions(null));
        });

        it("groups by `d.question.sectionName`", () => {
            const q1 = {sectionName: "s1" };
            const q2 = {sectionName: "s1" };
            const q3 = {sectionName: "s2" };
            const q4 = {};
            const qs = [q1, q2, q3, q4];

            assert.isArray(groupQuestions(qs));
            assert.equal(groupQuestions(qs).length, 3);
            assert.sameDeepMembers(
                groupQuestions(qs),
                [
                    {sectionName: "s1", questions: [q1, q2]},
                    {sectionName: "s2", questions: [q3]},
                    {sectionName: "Other", questions: [q4]}
                ]);
        });

        it("uses a default section name of 'Other' if not specified", () => {
            const q1 = {};
            const qs = [q1];

            assert.sameDeepMembers(
                groupQuestions(qs),
                [
                    {sectionName: "Other", questions: [q1]}
                ]);
        });
    });
});