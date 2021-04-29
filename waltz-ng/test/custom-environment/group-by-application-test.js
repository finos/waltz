import {assert} from "chai";
import {groupUsagesByApplication} from "../../client/technology/svelte/custom-environment-panel/custom-environment-utils";
import _ from "lodash";

const app1 = {
    id: 1
}

const app2 = {
    id: 2
}

const server1 = {
    entityReference: {kind: "SERVER_USAGE", id: 1}
}

const server2 = {
    entityReference: {kind: "SERVER_USAGE", id: 2}
}

const database1 = {
    entityReference: {kind: "DATABASE", id: 1}
}

const emptyList= [];
const listOneEntry = [{owningApplication: app1, usage: server1}];
const listMultipleEntriesSharingSameApp = [
    {owningApplication: app1, usage: server1},
    {owningApplication: app1, usage: server2},
    {owningApplication: app1, usage: database1}]


const listMultipleApps = [
    {owningApplication: app1, usage: server1},
    {owningApplication: app2, usage: server2}];


describe("technology/svelte/custom-environment-panel/custom-environment-utils", () => {
    describe("groupUsagesByApplication - empty cases", () => {
        it("gives back empty list if no usages",
           () => assert.isEmpty(groupUsagesByApplication(emptyList), "List is not empty"));
        it("gives back empty list if null parameter given",
           () => assert.isEmpty(groupUsagesByApplication(null), "Expected empty list if given null"));
    });
    describe("groupUsagesByApplication - normal cases", () => {
        it("gives back list containing one server usage if only one usage given",
           () => {
               const result = groupUsagesByApplication(listOneEntry);
               assert.lengthOf(result, 1, "List should have one entry");

               const firstResult = result[0];
               assert.deepEqual(firstResult.application, app1, "Result app should be equal to usage app");

               assert.lengthOf(firstResult.serverUsages, 1, "Should have one server usage");
               assert.lengthOf(firstResult.databaseUsages, 0, "Should have no database usage");
           });
        it("gives grouped usages by application (same app all usages)",
           () => {
               const result = groupUsagesByApplication(listMultipleEntriesSharingSameApp);
               assert.lengthOf(result, 1, "List should have one entry");

               const firstResult = result[0];
               assert.deepEqual(firstResult.application, app1, "Result app should be equal to usage app");

               assert.lengthOf(firstResult.serverUsages, 2, "Should have server usages ");
               assert.lengthOf(firstResult.databaseUsages, 1, "Should have database usages");

               assert.includeDeepMembers(
                   firstResult.serverUsages,
                   [{owningApplication: app1, usage: server1},
                       {owningApplication: app1, usage: server2}]);

               assert.includeDeepMembers(
                   firstResult.databaseUsages,
                   [{owningApplication: app1, usage: database1}]);
           });
        it("gives grouped usages by application",
           () => {
               const result = groupUsagesByApplication(listMultipleApps);
               assert.lengthOf(result, 2, "List should have two entries");

               const app1Result = _.find(result, d => d.application.id === 1, null);
               assert.deepEqual(app1Result.application, app1, "Result app should be equal to usage app");

               const app2Result = _.find(result, d => d.application.id === 2, null);
               assert.deepEqual(app2Result.application, app2, "Result app should be equal to usage app");

               assert.lengthOf(app1Result.serverUsages, 1, "Should have server usages ");
               assert.lengthOf(app2Result.serverUsages, 1, "Should have server usages");

               assert.deepEqual(app1Result.serverUsages[0], {owningApplication: app1, usage: server1});
               assert.deepEqual(app2Result.serverUsages[0], {owningApplication: app2, usage: server2});
           });
    })
})