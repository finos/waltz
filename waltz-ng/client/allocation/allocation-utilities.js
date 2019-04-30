import _ from "lodash";


export function calcWorkingTotal(enrichedAllocations = []) {
    return _.sumBy(
        enrichedAllocations,
        d => _.result(d, ["working", "percentage"], () => console.log("Allocation has no working percentage, defaulting to 0",d) || 0));
}


export function validateItems(items = []) {
    const dirtyItems =  _.filter(items, d => d.working.dirty);
    if (_.isEmpty(items) && !_.isEmpty(dirtyItems)) {
        // nothing to check
        return true;
    }

    updateValidations(items);

    const saveEnabled = _.every(items, d => d.working.status !== "FAIL");
    return saveEnabled;
}


function updateValidations(items = []) {
    const totalFixed = _.sumBy(items, fa => fa.working.percentage);
    _.each(items, d => updateValidationForItem(d, totalFixed));
}


function updateValidationForItem(d, totalFixed) {
    if (d.working.percentage > 100) {
        d.working.status = "FAIL";
        d.working.message = "Cannot exceed 100%";
    } else if (d.working.percentage < 0) {
        d.working.status = "FAIL";
        d.working.message = "Cannot be less than 0%";
    } else if (totalFixed > 100) {
        d.working.status = "FAIL";
        d.working.message = "Total exceeds 100%";
    } else if (totalFixed < 100) {
        d.working.status = "WARN";
        d.working.message = "Total does not make 100%";
    } else {
        d.working.status = "OK";
        d.working.message = "";
    }
}

function updateDirtyFlag(item) {
    const changeType = determineChangeType(item);
    item.working.dirty = ! _.isEqual("NONE", changeType);
    return item;
}


export function updateDirtyFlags(items = []) {
    _.forEach(items, d => {
        updateDirtyFlag(d);
    });
    return _.some(items, d => d.working.dirty);
}


export function determineChangeType(d) {
    const currentlyAllocated = !_.isNil(d.allocation);
    const workingPercentage = _.get(d, ["working", "percentage"], 0);
    const currentPercentage = _.get(d, ["allocation", "percentage"], 0);

    if (!currentlyAllocated && d.working.isAllocated) {
        return "ADD";
    } else if (currentlyAllocated && !d.working.isAllocated) {
        return "REMOVE";
    } else if (workingPercentage !== currentPercentage) {
        return "UPDATE";
    } else {
        return "NONE";
    }
}