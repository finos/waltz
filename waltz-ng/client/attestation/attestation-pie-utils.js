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

/**
 * Returns count of applications for each attested category specified in the config
 * @param applications: all applications subjected for aggregation
 * @param attestationRuns: attestationRun for the applications
 * @param attestationInstances: all attestationInstances for the applications
 * @param attestedEntityKind: Entity Kind against which attestation data needs to be collected
 * **/
export function prepareSummaryData(applications = [],
                                   attestationRuns = [],
                                   attestationInstances = [],
                                   attestedEntityKind) {

    const attestedRunIdsFilteredByEntityKind =
        _.chain(attestationRuns)
            .filter(d => d.attestedEntityKind === attestedEntityKind)
            .map(d => d.id)
            .value();

    const attestedAppIds = getAttestedAppIds(attestationInstances, attestedRunIdsFilteredByEntityKind);

    const applicationsWithAttestationFlag =
        _.map(applications, app => addAttestedStatus(app, attestedAppIds));
    // console.log('application with attestation flag ', attestedAppIds, applicationsWithAttestationFlag);

    return toKeyCounts(applicationsWithAttestationFlag, a => a.isAttested);

    function addAttestedStatus(app, attestedAppIds) {
        return _.extend(app,
            {isAttested: _.includes(attestedAppIds, app.id) ? "ATTESTED" : "NEVER_ATTESTED"});

    }
}

function getAttestedAppIds(attestationInstances, attestedRunIds) {
    return _.chain(attestationInstances)
        .filter(instance => isAttested(instance)
            && _.includes(attestedRunIds, instance.attestationRunId))
        .map(instance => instance.parentEntity.id)
        .uniq()
        .value();

    function isAttested(instance) {
        return instance.attestedAt != null;
    }
}
