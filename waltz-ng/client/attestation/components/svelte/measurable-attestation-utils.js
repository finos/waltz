import _ from "lodash";

export function mergeCategoriesWithLatestAttestations(categories = [], latestAttestations = []) {
    const attestationsByCategoryId = _.keyBy(latestAttestations, d => d.categoryRef.id);

    return _
        .chain(categories)
        .map(d => Object.assign({}, d, {latestAttestation: attestationsByCategoryId[d.qualifierReference.id]}))
        .orderBy(d => d.qualifierReference.name)
        .value();
}