// utils/settings-utils.js
import {DATAFLOW_PROPOSAL_RATING_SCHEME_SETTING_NAME, DATAFLOW_PROPOSAL_SETTING_NAME} from "../constants";

export function isDataFlowProposalsEnabled(settings) {
    const setting = settings.find(s => s.name === DATAFLOW_PROPOSAL_SETTING_NAME);
    return setting?.value === 'true';
}

export function getDataFlowProposalsRatingScheme(settings) {
    const setting = settings.filter(t => t.name === DATAFLOW_PROPOSAL_RATING_SCHEME_SETTING_NAME)[0];
    return setting?.value;
}