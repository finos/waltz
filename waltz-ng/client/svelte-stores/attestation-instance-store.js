import {remote} from "./remote";
import {checkIsEntityRef} from "../common/checks";


export function mkAttestationInstanceStore() {

    const findByEntityRef = (ref, force = false) => {
        checkIsEntityRef(ref);

        return remote
            .fetchViewList(
                "GET",
                `api/attestation-instance/entity/${ref.kind}/${ref.id}`,
                null,
                {force});
    };

    const findLatestMeasurableAttestations = (ref, force = false) => {
        checkIsEntityRef(ref);

        return remote
            .fetchViewList(
                "GET",
                `api/attestation-instance/latest/measurable-category/entity/${ref.kind}/${ref.id}`,
                null,
                {force});
    };


    const reassignRecipients = () => {
        return remote.execute()
    }

    return {
        findByEntityRef,
        findLatestMeasurableAttestations
    };
}


export const attestationInstanceStore = mkAttestationInstanceStore();