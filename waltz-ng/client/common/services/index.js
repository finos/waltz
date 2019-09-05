/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import _ from "lodash";
import BaseLookupService from "./BaseLookupService";
import {enums} from "./enums"
import preventNavigationService from "./prevent-navigation-service";
import serviceBroker from "./service-broker";
import {CORE_API} from "./core-api-utils";
import {toMap} from "../map-utils";


const displayNameService = new BaseLookupService();
const iconNameService = new BaseLookupService();
const iconColorService = new BaseLookupService();
const descriptionService = new BaseLookupService();


function loadFromServer(involvementKindService,
                        serviceBroker) {

    serviceBroker
        .loadAppData(CORE_API.DataTypeStore.findAll)
        .then(result => {
            // DEPRECATED, should be byId
            const indexedByCode = _.keyBy(result.data, "code");
            const indexedById = _.keyBy(result.data, "id");

            displayNameService
                .register("dataType", _.mapValues(indexedByCode, "name"))
                .register("dataType", _.mapValues(indexedById, "name"))
                ;

            descriptionService
                .register("dataType", _.mapValues(indexedByCode, "description"))
                .register("dataType", _.mapValues(indexedById, "description"))
                ;
        });

    involvementKindService
        .loadInvolvementKinds()
        .then(results => {
            const indexedById = _.keyBy(results, "id");
            displayNameService.register("involvementKind", _.mapValues(indexedById, "name"));
            descriptionService.register("involvementKind", _.mapValues(indexedById, "description"));
        });

    serviceBroker
        .loadAppData(CORE_API.MeasurableCategoryStore.findAll)
        .then(result => {
            const indexedById = _.keyBy(result.data, "id");
            displayNameService.register("measurableCategory", _.mapValues(indexedById, "name"));
            descriptionService.register("measurableCategory", _.mapValues(indexedById, "description"));
        });

    serviceBroker
        .loadAppData(CORE_API.EntityNamedNoteTypeStore.findAll)
        .then(result => {
            const indexedById = _.keyBy(result.data, "id");
            displayNameService.register("entityNamedNoteType", _.mapValues(indexedById, "name"));
            descriptionService.register("entityNamedNoteType", _.mapValues(indexedById, "description"));
        });

    serviceBroker
        .loadAppData(CORE_API.EnumValueStore.findAll)
        .then(r => {
            const keyFn = x => x.key;
            _.chain(r.data)
                .groupBy("type")
                .each((xs, type) => {
                    displayNameService.register(type, toMap(xs, keyFn, x => x.name));
                    descriptionService.register(type, toMap(xs, keyFn, x => x.description));
                    iconNameService.register(type, toMap(xs, keyFn, x => x.icon));
                    iconColorService.register(type, toMap(xs, keyFn, x => x.iconColor));
                })
                .value();
        });
}


export default (module) => {
    module
        .service("DisplayNameService", () => displayNameService)
        .service("IconNameService", () => iconNameService)
        .service("IconColorService", () => iconColorService)
        .service("DescriptionService", () => descriptionService)
        .service("PreventNavigationService", preventNavigationService)
        .service("ServiceBroker", serviceBroker);

    const keyFn = x => x.key;
    _.each(enums, (xs, type) => {
        displayNameService.register(type, toMap(xs, keyFn, x => x.name));
        descriptionService.register(type, toMap(xs, keyFn, x => x.description));
        iconNameService.register(type, toMap(xs, keyFn, x => x.icon));
        iconColorService.register(type, toMap(xs, keyFn, x => x.iconColor));
    });

    loadFromServer.$inject = [
        "InvolvementKindService",
        "ServiceBroker"
    ];


    function configServiceBroker($transitions, serviceBroker) {
        $transitions.onEnter({}, (transition, state, opts) => {

            const promise = serviceBroker
                .loadViewData(CORE_API.ClientCacheKeyStore.findAll)
                .then(r => r.data)
                .then(oldCacheKeys => {
                    serviceBroker.resetViewData();

                    return serviceBroker
                        .loadViewData(
                            CORE_API.ClientCacheKeyStore.findAll,
                            [],
                            {force: true})
                        .then(r => r.data)
                        .then(newCacheKeys => {
                            const differences = _.differenceWith(newCacheKeys, oldCacheKeys, _.isEqual);
                            if (differences.length > 0) {
                                serviceBroker.resetAppData();
                            }
                        });
                });
            transition.promise.finally(promise);
        });
    }

    configServiceBroker.$inject = [
        "$transitions",
        "ServiceBroker"
    ];

    module
        .run(loadFromServer)
        .run(configServiceBroker);
};
