import {cmp} from "./sort-utils";
import EntityLink from "./svelte/EntityLink.svelte";
import {applicationKind} from "./services/enums/application-kind";
import {lifecyclePhase} from "./services/enums/lifecycle-phase";


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


function mkRatingSchemeItemFormatter(labelProvider = v => v?.ratingSchemeItem.name) {
    return (row, cell, value) => {
        const ratingSchemeItem = _.get(value, ["ratingSchemeItem"]);
        const label = labelProvider(value) || "";
        const style = ratingSchemeItem
            ? `border-left: solid 2px ${ratingSchemeItem.color}; padding-left: 0.2em;`
            : '';
        return `
            <div style='${style}'>
                ${label}
            </div>`;
    };
}


function mkRatingSchemeItemsFormatter(labelProvider = v => v?.ratingSchemeItem.name) {
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


export function mkAssessmentAndCategoryColumns(assessmentDefs = [], categories = []) {
    const assessmentCols = _
        .chain(assessmentDefs)
        .sortBy(d => d.name)
        .map(d => {
            const field = "assessment_definition/" + d.externalId;
            return {
                id: d.externalId,
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
            const field = "measurable_category/" + d.externalId;
            return {
                id: d.externalId,
                name: d.name,
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