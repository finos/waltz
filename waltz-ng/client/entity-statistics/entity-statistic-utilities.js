import {rollupKindNames} from '../common/services/display_names';

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
    return rollupKindNames[definition.rollupKind] || '-';
}
