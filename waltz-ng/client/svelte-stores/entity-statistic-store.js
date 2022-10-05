import {remote} from "./remote";


export function mkEntityStatisticStore() {

    const findAllActiveDefinitions = (force) => remote
        .fetchAppList(
            "GET",
            "api/entity-statistic/definition",
            null,
            {force});

    return {
        findAllActiveDefinitions,
    };
}


export const entityStatisticStore = mkEntityStatisticStore();