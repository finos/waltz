// utils/settings-utils.js
import {DATAFLOW_PROPOSAL_RATING_SCHEME_SETTING_NAME, DATAFLOW_PROPOSAL_SETTING_NAME} from "../constants";

export function isDataFlowProposalsEnabled(settingsSvc) {
    return settingsSvc
        .findOrDefault(DATAFLOW_PROPOSAL_SETTING_NAME, "false")
        .then(v => v === "true");
}

export function getDataFlowProposalsRatingScheme(settingsSvc) {
    return settingsSvc
        .findOrDefault(DATAFLOW_PROPOSAL_RATING_SCHEME_SETTING_NAME, undefined)
        .then(v => v);
}