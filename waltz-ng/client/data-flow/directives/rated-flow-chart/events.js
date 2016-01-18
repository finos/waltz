export const EventTypes = {
    APP_RATING_CELL_SELECTED: 'rated-flow-chart-app-type-cell-selected',
    APP_SELECTED: 'rated-flow-chart-app-selected',
    ORG_UNIT_SELECTED: 'rated-flow-chart-org-unit-selected',
    ORG_UNIT_RATING_SELECTED: 'rated-flow-chart-org-unit-rating-selected'
};


export function appRatingCellSelected(data) {
    return { type: EventTypes.APP_RATING_CELL_SELECTED, data };
}


export function appSelected(data) {
    return { type: EventTypes.APP_SELECTED, data };
}


export function orgUnitSelected(data) {
    return { type: EventTypes.ORG_UNIT_SELECTED, data };
}

export function orgUnitRatingSelected(data) {
    return { type: EventTypes.ORG_UNIT_RATING_SELECTED, data };
}
