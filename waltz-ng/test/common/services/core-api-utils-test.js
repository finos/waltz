import _ from "lodash";
import {assert} from "chai";
import {CORE_API} from "../../../client/common/services/core-api-utils";


describe("CoreApiUtils: checker", () => {

    it("all exported function service names match exported API name", () => {

        const exportedApiNames = _.keys(CORE_API);
        _.forEach(exportedApiNames, exportedApiName => {

            const api = CORE_API[exportedApiName];
            _.forEach(api, a => {
                assert.equal(
                    a.serviceName,
                    exportedApiName,
                    `Exported API: ${exportedApiName}, does not match function service name: ${a.serviceName}`);
            })

        });
    });
});

