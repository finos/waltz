import {rollupKindNames} from "../common/services/display_names";

export function updateUrlWithoutReload($state, navItem) {
    $state.go('.', {id: navItem.id}, {notify: false});
}


const defaultDefinitions = {
    children: [],
    parent: null
};


export function hasRelatedDefinitions(definitions = defaultDefinitions) {
    return definitions.children.length > 0;
}


export function navigateToStatistic($state, statisticId, parentEntityReference) {
    const params = {
        id: parentEntityReference.id,
        kind: parentEntityReference.kind,
        statId: statisticId
    };

    const stateName = parentEntityReference.kind === 'PERSON'
        ? "main.entity-statistic.view-person"
        : "main.entity-statistic.view";

    $state.go(stateName, params);
}


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



