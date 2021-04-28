import {assert} from "chai";
import {termSearch} from "../../client/common";

const item1 = {name: "item1", description: "description", ignore: "ignored"};
const item2 = {name: "item2", description: "similar description", ignore: "ignored"};
const emptyList = [];

const itemList = [item1, item2];

describe("TermSearch", () => {
    describe("termSearch - empty cases", () => {
        it("gives back empty list if no items to search over",
           () => assert.isEmpty(termSearch(emptyList), "List is not empty"));
        it("gives back original list if no search string or inclusion callback provided",
           () => assert.deepEqual(termSearch(itemList), itemList, "Expected original list"));
    });
    describe("termSearch - search over one field", () => {
        it("if field exists and contains string returns item",
           () => {
               assert.isEmpty(termSearch(itemList, "description", ["noSuchField"]), "List should be empty as field doesn't exist");
               assert.deepEqual(termSearch(itemList, "description", ["description"]), itemList, "All items should be returned");
               assert.deepEqual(termSearch(itemList, "Description", ["description"]), itemList, "All items should be returned");
               assert.deepEqual(termSearch(itemList, "similar", ["description"]), [item2], "Only item2 should be returned as item1 does not contain search string");
           });
    });
    describe("termSearch - results can be omitted", () => {
        it("if field exists and contains string returns item",
           () => {
               assert.isEmpty(termSearch(itemList, "similar", ["description"], d => d.name === "item2"), "List should be empty as field doesn't exist");
               assert.deepEqual(termSearch(itemList, "description", ["description"], d => d.name === "item2"),
                                [item1], 
                                "List should be empty as field doesn't exist");
           });
    });
})