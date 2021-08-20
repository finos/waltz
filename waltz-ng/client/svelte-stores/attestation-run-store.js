import {remote} from "./remote";
import {checkIsEntityRef} from "../common/checks";


export function mkAttestationRunStore() {

    const findByEntityRef = (ref, force = false) => {
        checkIsEntityRef(ref);

        return remote
            .fetchViewList(
                "GET",
                `api/attestation-run/entity/${ref.kind}/${ref.id}`,
                null,
                {force});
    };

    return {
        findByEntityRef
    };
}


export const attestationRunStore = mkAttestationRunStore();