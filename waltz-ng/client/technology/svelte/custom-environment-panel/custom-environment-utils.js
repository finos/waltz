import _ from "lodash";

export function groupUsagesByApplication(usages) {
    return _
        .chain(usages)
        .groupBy(u => u.owningApplication.id)
        .map((v, k) => {
            const application = _.head(v).owningApplication;
            const [serverUsages, databaseUsages] = _
                .chain(v)
                .partition(d => d.asset.hostname != null)
                .value();
            return Object.assign({}, {application, serverUsages, databaseUsages})
        })
        .value();
}
