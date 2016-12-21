/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";


export const applicationKindDisplayNames = {
    CUSTOMISED: 'Customised',
    EUC: 'End User Computing',
    EXTERNAL: 'External to the organisation',
    EXTERNALLY_HOSTED: 'Externally Hosted',
    IN_HOUSE: 'In House',
    INTERNALLY_HOSTED: 'Hosted Internally',
    THIRD_PARTY: 'Third Party'
};


export const authSourceRatingNames = {
    PRIMARY: 'Primary Source',
    SECONDARY: 'Secondary Source',
    NO_OPINION: 'No Opinion',
    DISCOURAGED: 'Discouraged Source',
    NOT_APPLICABLE: 'N/A'
};


export const bookmarkNames = {
    APPLICATION_INSTANCE: 'Application Instance',
    BUILD_SYSTEM: 'Automated Build / Deployment System',
    BUSINESS_SPECIFICATION: 'Business Specification',
    DOCUMENTATION: 'Documentation',
    ISSUE_TRACKER: 'Issue Tracking',
    MONITORING: 'Monitoring Systems',
    QUALITY_CONTROL: 'Quality Control',
    SOURCE_CODE_CONTROL: 'Source Code Control',
    TECHNICAL_SPECIFICATION: 'Technical Specification',
};


export const investmentRatingNames = {
    R: 'Disinvest',
    A: 'Maintain',
    G: 'Invest',
    Z: 'Unknown'
};


export const capabilityRatingNames = investmentRatingNames;


export const applicationRatingNames = investmentRatingNames;


export const lifecyclePhaseDisplayNames = {
    PRODUCTION: 'Production',
    DEVELOPMENT: 'Development',
    CONCEPTUAL: 'Conceptual',
    RETIRED: 'Retired'
};


export const orgUnitKindNames = {
    IT: 'Information Technology',
    BUSINESS: 'Business',
    CONTROL: 'Control'
};


export const severityNames = {
    INFORMATION: 'Info',
    ERROR: 'Error',
    WARNING: 'Warning'
};


export const entityNames = {
    ACTOR: 'Actor',
    APP_CAPABILITY: 'Application Function',
    APP_GROUP: 'Application Group',
    APP_RATING: 'Application Rating',
    APPLICATION: 'Application',
    ASSET_COST: 'Asset Cost',
    AUTHORITATIVE_SOURCE: "Authoritative Source",
    BOOKMARK: 'Bookmark',
    CAPABILITY: 'Function',
    CHANGE_INITIATIVE: 'Change Initiative',
    DATABASE: 'Database',
    DATA_TYPE: 'Data Type',
    END_USER_APPLICATION: 'End User App',
    ENTITY_STATISTIC: 'Statistic',
    INVOLVEMENT: 'Involvement',
    LOGICAL_DATA_FLOW: "Logical Data Flow",
    ORG_UNIT: 'Org Unit',
    MEASURABLE: 'Measurable',
    PERSON: 'Person',
    PHYSICAL_SPECIFICATION: 'Physical Specification',
    PHYSICAL_FLOW: 'Physical Flow',
    PROCESS: 'Process',
    SERVER: 'Server',
    SOFTWARE: 'Software',
    SYSTEM: 'System'
};


export const changeInitiativeNames = {
    PROGRAMME: 'Programme',
    PROJECT: 'Project',
    INITIATIVE: 'Initiative'
};


export const assetCostKindNames = {
    APPLICATION_DEVELOPMENT: 'Application Development',
    INFRASTRUCTURE: 'Infrastructure',
    PEOPLE: 'People Costs'
};


export const entityStatisticCategoryDisplayNames = {
    COMPLIANCE: 'Compliance',
    GOVERNANCE: 'Governance',
    SECURITY: 'Security',
    REGULATORY: 'Regulatory',
    DATA_QUALITY: 'Data Quality'
};


export const usageKindDisplayNames = {
    ORIGINATOR: 'Originator',
    DISTRIBUTOR: 'Distributor',
    CONSUMER: 'Consumer',
    MODIFIER: 'Modifier'
};


export const criticalityDisplayNames = {
    NONE: 'None',
    LOW: 'Low',
    MEDIUM: 'Medium',
    HIGH: 'High',
    VERY_HIGH: 'Very high',
    UNKNOWN: 'Unknown'
};


export const rollupKindNames = {
    SUM_BY_VALUE: "Sum",
    AVG_BY_VALUE: "Average",
    COUNT_BY_ENTITY: "Count",
    NONE: "Value"
};


export const endOfLifeStatusNames = {
    'END_OF_LIFE': 'End of Life',
    'NOT_END_OF_LIFE': 'Compliant'
};


export const transportKindNames  = {
    DATABASE_CONNECTION: "Database Connection",
    EMAIL: "Email",
    FILE_TRANSPORT: "File Transport",
    FILE_SHARE: "File Share",
    MESSAGING: "Messaging",
    OTHER: "Other",
    RPC: "Remote Procedure Call",
    UDP: 'UDP',
    UNKNOWN: 'Unknown',
    WEB: "Web"
};


export const frequencyKindNames  = {
    ON_DEMAND: "On Demand",
    REAL_TIME: "Real Time",
    INTRA_DAY: "Intra-day",
    DAILY: "Daily",
    WEEKLY: "Weekly",
    MONTHLY: "Monthly",
    QUARTERLY: "Quarterly",
    BIANNUALLY: "Biannually",
    YEARLY: "Yearly",
    UNKNOWN: 'Unknown'
};


export const dataFormatKindNames  = {
    BINARY: "Binary",
    DATABASE: "Database",
    FLAT_FILE: "Flat File",
    JSON: "JSON",
    OTHER: "Other",
    UNKNOWN: 'Unknown',
    UNSTRUCTURED: "Unstructured",
    XML: "XML",
};


export const lifecycleStatusNames = {
    'ACTIVE': 'Active',
    'BUILDING': 'Building',
    'INACTIVE': 'Inactive',
    'RETIRING': 'Retiring',
    'UNKNOWN': 'Unknown'
};


export const measurableKindNames = {
    BUSINESS_LINE: 'Business Line',
    CAPABILITY: 'Function',
    PROCESS: 'Process',
    PRODUCT: 'Product',
    REGION: 'Region',
    SERVICE: 'Service'
};


/**
 * Used to convert a map of ( { code -> displayName }
 * @param lookups
 * @param excludeUnknown
 */
export function toOptions(lookups = {}, excludeUnknown = false) {
    return _.chain(lookups)
        .map((v, k) => ({ name: v, code: k}))
        .sortBy(o => o.name)
        .reject(o => o.code === 'UNKNOWN' && excludeUnknown)
        .value();
}


/**
 * Used to convert a map of { code->displayName } into
 * a format suitable for use by ui-grid.
 * @param lookups
 */
export function toGridOptions(lookups = {}) {
    return _.map(
        lookups,
        (v, k) => ({ label: v, value: k}));
}
