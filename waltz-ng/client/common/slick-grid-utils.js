import {cmp, compareDates} from "./sort-utils";
import EntityLink from "./svelte/EntityLink.svelte";
import {applicationKind} from "./services/enums/application-kind";
import {lifecyclePhase} from "./services/enums/lifecycle-phase";
import _ from "lodash";


export function mkSortFn(sortCol, sortAsc = true) {
    return (a, b) => {
        const result = sortCol.sortFn
            ? sortCol.sortFn(a, b)
            : cmp(a[sortCol.field], b[sortCol.field])

        return sortAsc ? result : -result;
    }
}


export function mkEntityLinkFormatter(valueProvider, showIcon = true) {
    return (row, cell, value, colDef, dataCtx) => {
        const me = document.createElement("span");
        const component = new EntityLink({target: me});

        const ref = valueProvider
            ? valueProvider(value, dataCtx)
            : value;

        component.$set({ref, showIcon});
        return me;
    }
}


export function mkExternalIdFormatter(valueProvider = d => d.externalId) {
    return (row, cell, value, colDef, dataCtx) => {
        const extId = valueProvider(value, dataCtx);
        return `<span>${extId}</span>`;
    }
}


export function mkEnumFormatter(valueProvider, enums) {

    return (row, cell, value, colDef, dataCtx) => {
        const enumName = valueProvider
            ? valueProvider(value, dataCtx)
            : value;
        return `<span>${enums[enumName]?.name || enumName}</span>`;
    };
}


export function mkApplicationKindFormatter(valueProvider) {
    return mkEnumFormatter(valueProvider, applicationKind);
}


export function mkLifecyclePhaseFormatter(valueProvider) {
    return mkEnumFormatter(valueProvider, lifecyclePhase);
}


export function mkAllocationFormatter(labelProvider = v => v?.percentage) {
    return (row, cell, value) => {
        const label = labelProvider(value);
        return _.isEmpty(value) ? `<span>-</span>` : `<span>${label}%</span>`;
    };
}


export function mkDecommFormatter(labelProvider = v => v?.plannedDecommissionDate) {
    return (row, cell, value) => {
        const label = labelProvider(value);
        return _.isEmpty(label) ? null : `<span>${label}</span>`;
    };
}


export function mkReplacementAppsFormatter(labelProvider = v => v?.entityReference.name) {
    return (row, cell, values) => {
        return _
            .chain(values)
            .orderBy(value => _.toLower(labelProvider(value)))
            .map(value => {
                const label = labelProvider(value) || "";
                return `<span>${label}</span>`;
            })
            .join("<span>, </span>")
            .value();
    }
}


export function mkRatingSchemeItemsFormatter(labelProvider = v => v?.ratingSchemeItem.name) {
    return (row, cell, values) => {
        return _
            .chain(values)
            .map(value => {
                const ratingSchemeItem = _.get(value, ["ratingSchemeItem"]);
                const label = labelProvider(value) || "";
                const style = ratingSchemeItem
                    ? `border-left: solid 2px ${ratingSchemeItem.color}; padding-left: 0.2em;`
                    : '';
                return `
                    <span style='${style}'>
                        ${label}
                    </span>`;
            })
            .join(" ")
            .value();
    };
}


export function mkRatingSchemeItemFormatter(labelProvider = v => v?.ratingSchemeItem.name,
                                            ratingProvider = v => v?.ratingSchemeItem) {
    return (row, cell, value) => {
        const rating = ratingProvider(value);
        const label = labelProvider(value) || "";
        const style = rating
            ? `border-left: solid 2px ${rating.color}; padding-left: 0.2em;`
            : '';
        return `
            <span style='${style}'>
                ${label}
            </span>`;
    };
}


export function mkPrimaryAssessmentAndCategoryColumns(assessmentDefs = [], categories = []) {
    const assessmentCols = _
        .chain(assessmentDefs)
        .sortBy(d => d.name)
        .map(d => {
            const field = "assessment_definition/" + d.id;
            return {
                id: d.id,
                name: d.name,
                field,
                sortable: true,
                formatter: mkRatingSchemeItemsFormatter(),
                width: 130,
                sortFn: (a, b) => cmp(
                    _.get(a, [field, 0, "ratingSchemeItem", "name"], ""),
                    _.get(b, [field, 0, "ratingSchemeItem", "name"], ""))
            };
        })
        .value();

    const categoryCols = _
        .chain(categories)
        .sortBy(d => d.name)
        .map(d => {
            const field = "measurable_category/" + d.id;
            return {
                id: d.id,
                name: `Primary ${d.name}`,
                width: 150,
                field,
                sortable: true,
                formatter: mkRatingSchemeItemFormatter(v => v?.measurable.name),
                sortFn: (a, b) => cmp(
                    _.get(a, [field, "measurable", "name"], ""),
                    _.get(b, [field, "measurable", "name"], ""))
            };
        })
        .value();

    return _.concat(
        categoryCols,
        assessmentCols);
}


export function mkAllocationColumns(allocationSchemes = []) {
    return _
        .chain(allocationSchemes)
        .sortBy(d => d.name)
        .map(d => {
            const field = "allocation_scheme/" + d.id;
            return {
                id: d.id,
                name: d.name,
                field,
                sortable: true,
                formatter: mkAllocationFormatter(),
                width: 130,
                sortFn: (a, b) => {
                    const v1 = _.get(a, [field, "percentage"], null);
                    const v2 = _.get(b, [field, "percentage"], null);
                    if(!_.isEmpty(v1) && _.isEmpty(v2)) {
                        return 1;
                    } else if (_.isEmpty(v1) && !_.isEmpty(v2)) {
                        return -1;
                    } else {
                        return cmp(v1, v2);
                    }
                }
            };
        })
        .value();
}


export function mkDecommissionColumns(plannedDecommissions = [], plannedReplacements = [], replacingDecommissions = []) {

    const decomColumns = [];

    if (!_.isEmpty(plannedDecommissions)) {
        const plannedDecomCol = {
            id: "planned_decommission_date",
            name:"Planned Decommission date",
            width: 150,
            field: "plannedDecommission",
            sortable: true,
            formatter: mkDecommFormatter(),
            sortFn: (a, b) => {
                const d1 = _.get(a, ["plannedDecommission", "plannedDecommissionDate"], null)
                const d2 = _.get(b, ["plannedDecommission", "plannedDecommissionDate"], null)
                return compareDates(d1, d2);
            }
        }
        decomColumns.push(plannedDecomCol);
    }

    if(!_.isEmpty(plannedReplacements)) {
        const plannedReplacementCol = {
            id: "replacement_applications",
            name:"Replacement Applications",
            width: 150,
            field: "replacementApplications",
            sortable: false,
            formatter: mkReplacementAppsFormatter(),
        }
        decomColumns.push(plannedReplacementCol);
    }
    return decomColumns;
}