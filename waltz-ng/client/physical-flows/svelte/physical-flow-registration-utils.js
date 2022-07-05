import _ from "lodash";

export let basisOffsetDefaultOptions = [
    {code: "-30", name: "T-30"},
    {code: "-10", name: "T-10"},
    {code: "-7", name: "T-7"},
    {code: "-5", name: "T-5"},
    {code: "-3", name: "T-3"},
    {code: "-1", name: "T-1"},
    {code: "0", name: "T"},
    {code: "1", name: "T+1"},
    {code: "3", name: "T+3"},
    {code: "5", name: "T+5"},
    {code: "7", name: "T+7"},
    {code: "10", name: "T+10"},
    {code: "30", name: "T+30"},
    {code: "OTHER", name: "Other (Please specify)"},
];

export const sections = {
    ROUTE: "ROUTE",
    SPECIFICATION: "SPECIFICATION",
    FLOW: "FLOW"
}


export function determineExpandedSections(expandedSections, section) {
    const alreadyExpanded = _.includes(expandedSections, section);
    if (alreadyExpanded) {
        return _.without(expandedSections, section);
    } else {
        return _.concat(expandedSections, section);
    }
}