import {attestationStatusColorScale} from "../common/colors";
import _ from "lodash";
import {toKeyCounts} from "../common";

const attestationStatus = {
    ATTESTED: {
        key: "ATTESTED",
        name: "Attested",
        icon: null,
        description: null,
        position: 10
    },
    NEVER_ATTESTED: {
        key: "NEVER_ATTESTED",
        name: "Never Attested",
        icon: null,
        description: null,
        position: 20
    }
};


export const attestationPieConfig = {
    colorProvider: (d) => attestationStatusColorScale(d.key),
    size: 40,
    labelProvider: (d) => attestationStatus[d.key] ? attestationStatus[d.key].name : "Unknown"
};


export function prepareSummaryData(entities = [], attestations = [], filterByEntityKind, attestedEntityKind) {
    const attestationsByAppId =
        _.chain(attestations)
        .filter(att =>
            att.selectionOptions.entityReference.kind === filterByEntityKind
            && att.attestedEntityKind === attestedEntityKind)
        .keyBy(att => att.selectionOptions.entityReference.id)
        .value();

    const allApps =
        _.map(entities, app =>
            _.extend(app,
            {isAttested: getAttestedStatus(attestationsByAppId, app)}));
    const entityCountByAttestation = toKeyCounts(allApps, a => a.isAttested);
    //following is not necesary if don't display keys with count 0
    return _.map(_.keys(attestationStatus), key =>
        Object.assign({key: key, count: getAttestationCount(entityCountByAttestation, key)}));
}

function getAttestedStatus(attestationsByAppId, app) {
    return _.has(attestationsByAppId, app.id) ? "ATTESTED" : "NEVER_ATTESTED";
}

function getAttestationCount(data, key) {
    const obj = _.find(data, d => d.key === key);
    return obj != null? obj.count : 0;

}