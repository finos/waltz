import _ from "lodash";

export function calcWorkingTotal(enrichedAllocations = []) {
    return _.sumBy(
        enrichedAllocations,
        d => _.result(d, ["working", "percentage"], () => console.log("Allocation has no working percentage, defaulting to 0",d) || 0));
}


export function validateAllocations(fixedAllocations = [],
                                    floatingAllocations = []) {
    let saveEnabled = true;
    const hasFloats = floatingAllocations.length > 0;
    const totalFixed = _.sumBy(fixedAllocations, fa => fa.working.percentage);

    _.each(fixedAllocations, (fa) => {
        if (fa.working.percentage > 100) {
            fa.working.status = "FAIL";
            fa.working.message = "Cannot exceed 100%";
            saveEnabled = false;
        } else if (fa.working.percentage < 0) {
            fa.working.status = "FAIL";
            fa.working.message = "Cannot be less than 0%";
            saveEnabled = false;
        } else if (totalFixed > 100) {
            fa.working.status = "WARN";
            fa.working.message = "Total exceeds 100%";
            saveEnabled = false;
        } else if (totalFixed < 100 && ! hasFloats) {
            fa.working.status = "WARN";
            fa.working.message = "Total does not make 100%";
            saveEnabled = false;
        } else {
            fa.working.status = "OK";
            fa.working.message = "";
        }
    });

    return saveEnabled;
}


export function updateDirtyFlags(enrichedAllocations = []) {
    let dirtyFound = false;
    _.forEach(enrichedAllocations, ea => {
        const percentageChanged = ea.working.percentage !== ea.allocation.percentage;
        const typeChanged = ea.working.type !== ea.allocation.type;
        const isDirty =  percentageChanged || typeChanged;
        ea.working.dirty = isDirty;
        dirtyFound = dirtyFound || isDirty;
    });
    return dirtyFound;
}


export function updateFloatingValues(floatingTotal = 0, floatingAllocations = []) {
    const total = _.clamp(floatingTotal, 0, 100);
    const perAllocation = floatingAllocations.length > 0
        ? total / floatingAllocations.length
        : 0;
    _.each(floatingAllocations, fa => fa.working.percentage = perAllocation);
    return floatingAllocations;
}