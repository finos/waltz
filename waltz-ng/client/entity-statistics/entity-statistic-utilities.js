export function mkSummaryTableHeadings(definition) {
    return [
        "Outcome",
        mkValueHeading(definition),
        "%"
    ];
}


function mkValueHeading(definition) {
    if (!definition) {
        return "";
    }

    switch (definition.rollupKind) {
        case "SUM_BY_VALUE": return "Sum";
        case "AVG_BY_VALUE": return "Average";
        case "COUNT_BY_ENTITY": return "Count";
        default:
            console.log("Unknown rollup kind", definition);
            return "-";
    }
}
