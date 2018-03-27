/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
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


export const action = {
    ADD: {
        key: 'ADD',
        name: 'Add',
        icon: null,
        description: null,
        position: 10
    },
    REMOVE: {
        key: 'REMOVE',
        name: 'Remove',
        icon: null,
        description: null,
        position: 10
    },
    NO_CHANGE: {
        key: 'NO_CHANGE',
        name: 'No change',
        icon: null,
        description: null,
        position: 10
    },
};


export const applicationKind = {
    CUSTOMISED: {
        key: 'CUSTOMISED',
        name: 'Customised',
        icon: null,
        description: null,
        position: 10
    },
    EUC: {
        key: 'EUC',
        name: 'End User Computing',
        icon: null,
        description: null,
        position: 20
    },
    EXTERNAL: {
        key: 'EXTERNAL',
        name: 'External to the organisation',
        icon: null,
        description: null,
        position: 30
    },
    EXTERNALLY_HOSTED: {
        key: 'EXTERNALLY_HOSTED',
        name: 'Externally Hosted',
        icon: null,
        description: null,
        position: 40
    },
    IN_HOUSE: {
        key: 'IN_HOUSE',
        name: 'In House',
        icon: null,
        description: null,
        position: 50
    },
    INTERNALLY_HOSTED: {
        key: 'INTERNALLY_HOSTED',
        name: 'Hosted Internally',
        icon: null,
        description: null,
        position: 60
    },
    THIRD_PARTY: {
        key: 'THIRD_PARTY',
        name: 'Third Party',
        icon: null,
        description: null,
        position: 70
    }
};


export const attestationType = {
    IMPLICIT: {
        key: 'IMPLICIT',
        name: 'Implicit',
        icon: null,
        description: null,
        position: 10
    },
    EXPLICIT: {
        key: 'EXPLICIT',
        name: 'Explicit',
        icon: null,
        description: null,
        position: 20
    }
};


export const BOOLEAN = {
    Unknown: {
        key: 'Unknown',
        name: 'Unknown',
        icon: 'question',
        description: null,
        position: 10
    },
    Exempt: {
        key: 'Exempt',
        name: 'Exempt',
        icon: 'ban',
        description: null,
        position: 20
    },
    Yes: {
        key: 'Yes',
        name: 'Yes',
        icon: 'check',
        description: null,
        position: 30
    },
    No: {
        key: 'No',
        name: 'No',
        icon: 'times',
        description: null,
        position: 40
    }
};


export const entityLifecycleStatus = {
    ACTIVE: {
        key: 'ACTIVE',
        name: 'Active',
        icon: null,
        description: null,
        position: 10
    },
    PENDING: {
        key: 'PENDING',
        name: 'Pending',
        icon: null,
        description: null,
        position: 20
    },
    REMOVED: {
        key: 'REMOVED',
        name: 'Removed',
        icon: null,
        description: null,
        position: 30
    }
};


export const investmentRating = {
    R: {
        key: 'R',
        name: 'Disinvest',
        icon: null,
        description: null,
        position: 10
    },
    A: {
        key: 'A',
        name: 'Maintain',
        icon: null,
        description: null,
        position: 20
    },
    G: {
        key: 'G',
        name: 'Invest',
        icon: null,
        description: null,
        position: 30
    },
    Z: {
        key: 'Z',
        name: 'Unknown',
        icon: null,
        description: null,
        position: 40
    }
};


export const capabilityRating = investmentRating;


export const applicationRating = investmentRating;


export const lifecyclePhase = {
    PRODUCTION: {
        key: 'PRODUCTION',
        name: 'Production',
        icon: null,
        description: null,
        position: 10
    },
    DEVELOPMENT: {
        key: 'DEVELOPMENT',
        name: 'Development',
        icon: null,
        description: null,
        position: 20
    },
    CONCEPTUAL: {
        key: 'CONCEPTUAL',
        name: 'Conceptual',
        icon: null,
        description: null,
        position: 30
    },
    RETIRED: {
        key: 'RETIRED',
        name: 'Retired',
        icon: null,
        description: null,
        position: 40
    }
};


export const orgUnitKind = {
    IT: {
        key: 'IT',
        name: 'Information Technology',
        icon: null,
        description: null,
        position: 10
    },
    BUSINESS: {
        key: 'BUSINESS',
        name: 'Business',
        icon: null,
        description: null,
        position: 20
    },
    CONTROL: {
        key: 'CONTROL',
        name: 'Control',
        icon: null,
        description: null,
        position: 30
    }
};


export const severity = {
    INFORMATION: {
        key: 'INFORMATION',
        name: 'Info',
        icon: 'sitemap',
        description: null,
        position: 10
    },
    ERROR: {
        key: 'ERROR',
        name: 'Error',
        icon: 'sitemap',
        description: null,
        position: 20
    },
    WARNING: {
        key: 'WARNING',
        name: 'Warning',
        icon: 'sitemap',
        description: null,
        position: 30
    }
};


export const entity = {
    ACTOR: {
        key: 'ACTOR',
        name: 'Actor',
        icon: 'user-circle',
        description: null,
        position: 10
    },
    APP_CAPABILITY: {
        key: 'APP_CAPABILITY',
        name: 'Application Function',
        icon: 'puzzle-piece',
        description: null,
        position: 20
    },
    APP_GROUP: {
        key: 'APP_GROUP',
        name: 'Application Group',
        icon: 'object-group',
        description: null,
        position: 30
    },
    APP_RATING: {
        key: 'APP_RATING',
        name: 'Application Rating',
        icon: 'star-o',
        description: null,
        position: 40
    },
    APPLICATION: {
        key: 'APPLICATION',
        name: 'Application',
        icon: 'desktop',
        description: null,
        position: 50
    },
    ASSET_COST: {
        key: 'ASSET_COST',
        name: 'Asset Cost',
        icon: 'money',
        description: null,
        position: 60
    },
    ATTESTATION: {
        key: 'ATTESTATION',
        name: 'Attestation',
        icon: 'check-square-o',
        description: null,
        position: 65
    },
    AUTHORITATIVE_SOURCE: {
        key: 'AUTHORITATIVE_SOURCE',
        name: 'Authoritative Source',
        icon: 'shield',
        description: null,
        position: 70
    },
    BOOKMARK: {
        key: 'BOOKMARK',
        name: 'Bookmark',
        icon: 'bookmark-o',
        description: null,
        position: 80
    },
    CHANGE_INITIATIVE: {
        key: 'CHANGE_INITIATIVE',
        name: 'Change Initiative',
        icon: 'paper-plane-o',
        description: null,
        position: 90
    },
    DATABASE: {
        key: 'DATABASE',
        name: 'Database',
        icon: 'database',
        description: null,
        position: 100
    },
    DATA_TYPE: {
        key: 'DATA_TYPE',
        name: 'Data Type',
        icon: 'qrcode',
        description: null,
        position: 110
    },
    DRILL_GRID_DEFINITION: {
        key: 'DRILL_GRID_DEFINITION',
        name: 'Drill Grid Definition',
        icon: 'compass',
        description: "A drill grid is used to compare the co-occurrence of two hierarchical dimensions of data",
        position: 110
    },
    END_USER_APPLICATION: {
        key: 'END_USER_APPLICATION',
        name: 'End User App',
        icon: 'table',
        description: null,
        position: 120
    },
    ENTITY_STATISTIC: {
        key: 'ENTITY_STATISTIC',
        name: 'Statistic',
        icon: 'pie-chart',
        description: null,
        position: 130
    },
    FLOW_DIAGRAM: {
        key: 'FLOW_DIAGRAM',
        name: 'Flow Diagram',
        icon: 'picture-o',
        description: null,
        position: 140
    },
    INVOLVEMENT: {
        key: 'INVOLVEMENT',
        name: 'Involvement',
        icon: 'share-alt-square',
        description: null,
        position: 150
    },
    LOGICAL_DATA_ELEMENT: {
        key: 'LOGICAL_DATA_ELEMENT',
        name: 'Logical Data Element',
        icon: 'asterisk',
        description: null,
        position: 155
    },
    LOGICAL_DATA_FLOW: {
        key: 'LOGICAL_DATA_FLOW',
        name: 'Logical Data Flow',
        icon: 'random',
        description: null,
        position: 160
    },
    ORG_UNIT: {
        key: 'ORG_UNIT',
        name: 'Org Unit',
        icon: 'sitemap',
        description: null,
        position: 170
    },
    MEASURABLE: {
        key: 'MEASURABLE',
        name: 'Viewpoint',
        icon: 'puzzle-piece',
        description: null,
        position: 180
    },
    MEASURABLE_RATING: {
        key: 'MEASURABLE_RATING',
        name: 'Viewpoint Rating',
        icon: undefined,
        description: null,
        position: 190
    },
    PERSON: {
        key: 'PERSON',
        name: 'Person',
        icon: 'user',
        description: null,
        position: 200
    },
    PHYSICAL_SPECIFICATION: {
        key: 'PHYSICAL_SPECIFICATION',
        name: 'Physical Specification',
        icon: 'file-text',
        description: null,
        position: 210
    },
    PHYSICAL_FLOW: {
        key: 'PHYSICAL_FLOW',
        name: 'Physical Flow',
        icon: 'qrcode',
        description: null,
        position: 220
    },
    PROCESS: {
        key: 'PROCESS',
        name: 'Process',
        icon: 'code-fork',
        description: null,
        position: 230
    },
    SERVER: {
        key: 'SERVER',
        name: 'Server',
        icon: 'server',
        description: null,
        position: 240
    },
    SOFTWARE: {
        key: 'SOFTWARE',
        name: 'Software',
        icon: 'gift',
        description: null,
        position: 250
    },
    SYSTEM: {
        key: 'SYSTEM',
        name: 'System',
        icon: 'gears',
        description: null,
        position: 260
    }
};


export const changeInitiative = {
    PROGRAMME: {
        key: 'PROGRAMME',
        name: 'Programme',
        icon: null,
        description: null,
        position: 10
    },
    PROJECT: {
        key: 'PROJECT',
        name: 'Project',
        icon: null,
        description: null,
        position: 20
    },
    INITIATIVE: {
        key: 'INITIATIVE',
        name: 'Initiative',
        icon: null,
        description: null,
        position: 30
    }
};


export const entityStatistic = {
    COMPLIANCE: {
        key: 'COMPLIANCE',
        name: 'Compliance',
        icon: 'exclamation-triangle',
        description: null,
        position: 10
    },
    GOVERNANCE: {
        key: 'GOVERNANCE',
        name: 'Governance',
        icon: 'balance-scale',
        description: null,
        position: 20
    },
    SECURITY: {
        key: 'SECURITY',
        name: 'Security',
        icon: 'unlock-alt',
        description: null,
        position: 30
    },
    REGULATORY: {
        key: 'REGULATORY',
        name: 'Regulatory',
        icon: 'registered',
        description: null,
        position: 40
    },
    DATA_QUALITY: {
        key: 'DATA_QUALITY',
        name: 'Data Quality',
        icon: 'exchange',
        description: null,
        position: 50
    },
    TECHNICAL: {
        key: 'TECHNICAL',
        name: 'Technical',
        icon: 'gears',
        description: null,
        position: 60
    }
};


export const usageKind = {
    ORIGINATOR: {
        key: 'ORIGINATOR',
        name: 'Originator',
        icon: 'pencil',
        description: null,
        position: 10
    },
    DISTRIBUTOR: {
        key: 'DISTRIBUTOR',
        name: 'Distributor',
        icon: 'paper-plane-o',
        description: null,
        position: 20
    },
    CONSUMER: {
        key: 'CONSUMER',
        name: 'Consumer',
        icon: 'eye',
        description: null,
        position: 30
    },
    MODIFIER: {
        key: 'MODIFIER',
        name: 'Modifier',
        icon: 'pencil-square-o',
        description: null,
        position: 40
    }
};


export const criticality = {
    NONE: {
        key: 'NONE',
        name: 'None',
        icon: null,
        description: null,
        position: 10
    },
    LOW: {
        key: 'LOW',
        name: 'Low',
        icon: null,
        description: null,
        position: 20
    },
    MEDIUM: {
        key: 'MEDIUM',
        name: 'Medium',
        icon: null,
        description: null,
        position: 30
    },
    HIGH: {
        key: 'HIGH',
        name: 'High',
        icon: null,
        description: null,
        position: 40
    },
    VERY_HIGH: {
        key: 'VERY_HIGH',
        name: 'Very high',
        icon: null,
        description: null,
        position: 50
    },
    UNKNOWN: {
        key: 'UNKNOWN',
        name: 'Unknown',
        icon: null,
        description: null,
        position: 60
    }
};


export const rag = {
    R: {
        key: 'R',
        name: 'R',
        icon: 'times-circle',
        description: null,
        position: 10
    },
    A: {
        key: 'A',
        name: 'A',
        icon: 'question-circle',
        description: null,
        position: 20
    },
    G: {
        key: 'G',
        name: 'G',
        icon: 'check-circle',
        description: null,
        position: 30
    },
    Z: {
        key: 'Z',
        name: 'Z',
        icon: 'circle-o',
        description: null,
        position: 40
    }
};


export const rollupKind = {
    SUM_BY_VALUE: {
        key: 'SUM_BY_VALUE',
        name: 'Sum',
        icon: null,
        description: null,
        position: 10
    },
    AVG_BY_VALUE: {
        key: 'AVG_BY_VALUE',
        name: 'Average',
        icon: null,
        description: null,
        position: 20
    },
    COUNT_BY_ENTITY: {
        key: 'COUNT_BY_ENTITY',
        name: 'Count',
        icon: null,
        description: null,
        position: 30
    },
    NONE: {
        key: 'NONE',
        name: 'Value',
        icon: null,
        description: null,
        position: 40
    }
};


export const endOfLifeStatus = {
    END_OF_LIFE: {
        key: 'END_OF_LIFE',
        name: 'End of Life',
        icon: null,
        description: null,
        position: 10
    },
    NOT_END_OF_LIFE: {
        key: 'NOT_END_OF_LIFE',
        name: 'Compliant',
        icon: null,
        description: null,
        position: 20
    }
};


export const transportKind = {
    DATABASE_CONNECTION: {
        key: 'DATABASE_CONNECTION',
        name: 'Database Connection',
        icon: null,
        description: null,
        position: 10
    },
    EMAIL: {
        key: 'EMAIL',
        name: 'Email',
        icon: null,
        description: null,
        position: 20
    },
    FILE_TRANSPORT: {
        key: 'FILE_TRANSPORT',
        name: 'File Transport',
        icon: null,
        description: null,
        position: 30
    },
    FILE_SHARE: {
        key: 'FILE_SHARE',
        name: 'File Share',
        icon: null,
        description: null,
        position: 40
    },
    MESSAGING: {
        key: 'MESSAGING',
        name: 'Messaging',
        icon: null,
        description: null,
        position: 50
    },
    OTHER: {
        key: 'OTHER',
        name: 'Other',
        icon: null,
        description: null,
        position: 60
    },
    RPC: {
        key: 'RPC',
        name: 'Remote Procedure Call',
        icon: null,
        description: null,
        position: 70
    },
    UDP: {
        key: 'UDP',
        name: 'UDP',
        icon: null,
        description: null,
        position: 80
    },
    UNKNOWN: {
        key: 'UNKNOWN',
        name: 'Unknown',
        icon: null,
        description: null,
        position: 90
    },
    WEB: {
        key: 'WEB',
        name: 'Web',
        icon: null,
        description: null,
        position: 100
    }
};


export const frequencyKind = {
    ON_DEMAND: {
        key: 'ON_DEMAND',
        name: 'On Demand',
        icon: null,
        description: null,
        position: 10
    },
    REAL_TIME: {
        key: 'REAL_TIME',
        name: 'Real Time',
        icon: null,
        description: null,
        position: 20
    },
    INTRA_DAY: {
        key: 'INTRA_DAY',
        name: 'Intra-day',
        icon: null,
        description: null,
        position: 30
    },
    DAILY: {
        key: 'DAILY',
        name: 'Daily',
        icon: null,
        description: null,
        position: 40
    },
    WEEKLY: {
        key: 'WEEKLY',
        name: 'Weekly',
        icon: null,
        description: null,
        position: 50
    },
    MONTHLY: {
        key: 'MONTHLY',
        name: 'Monthly',
        icon: null,
        description: null,
        position: 60
    },
    QUARTERLY: {
        key: 'QUARTERLY',
        name: 'Quarterly',
        icon: null,
        description: null,
        position: 70
    },
    BIANNUALLY: {
        key: 'BIANNUALLY',
        name: 'Biannually',
        icon: null,
        description: null,
        position: 80
    },
    YEARLY: {
        key: 'YEARLY',
        name: 'Yearly',
        icon: null,
        description: null,
        position: 90
    },
    UNKNOWN: {
        key: 'UNKNOWN',
        name: 'Unknown',
        icon: null,
        description: null,
        position: 100
    }
};


export const dataFormatKind = {
    BINARY: {
        key: 'BINARY',
        name: 'Binary',
        icon: null,
        description: null,
        position: 10
    },
    DATABASE: {
        key: 'DATABASE',
        name: 'Database',
        icon: null,
        description: null,
        position: 20
    },
    FLAT_FILE: {
        key: 'FLAT_FILE',
        name: 'Flat File',
        icon: null,
        description: null,
        position: 30
    },
    JSON: {
        key: 'JSON',
        name: 'JSON',
        icon: null,
        description: null,
        position: 40
    },
    OTHER: {
        key: 'OTHER',
        name: 'Other',
        icon: null,
        description: null,
        position: 50
    },
    UNKNOWN: {
        key: 'UNKNOWN',
        name: 'Unknown',
        icon: null,
        description: null,
        position: 60
    },
    UNSTRUCTURED: {
        key: 'UNSTRUCTURED',
        name: 'Unstructured',
        icon: null,
        description: null,
        position: 70
    },
    XML: {
        key: 'XML',
        name: 'XML',
        icon: null,
        description: null,
        position: 80
    }
};


export const lifecycleStatus = {
    ACTIVE: {
        key: 'ACTIVE',
        name: 'Active',
        icon: null,
        description: null,
        position: 10
    },
    BUILDING: {
        key: 'BUILDING',
        name: 'Building',
        icon: null,
        description: null,
        position: 20
    },
    INACTIVE: {
        key: 'INACTIVE',
        name: 'Inactive',
        icon: null,
        description: null,
        position: 30
    },
    RETIRING: {
        key: 'RETIRING',
        name: 'Retiring',
        icon: null,
        description: null,
        position: 40
    },
    UNKNOWN: {
        key: 'UNKNOWN',
        name: 'Unknown',
        icon: null,
        description: null,
        position: 50
    }
};


export const physicalSpecDefinitionFieldType = {
    DATE: {
        key: 'DATE',
        name: 'Date',
        icon: null,
        description: null,
        position: 10
    },
    DECIMAL: {
        key: 'DECIMAL',
        name: 'Decimal',
        icon: null,
        description: null,
        position: 20
    },
    INTEGER: {
        key: 'INTEGER',
        name: 'Integer',
        icon: null,
        description: null,
        position: 30
    },
    STRING: {
        key: 'STRING',
        name: 'String',
        icon: null,
        description: null,
        position: 40
    },
    BOOLEAN: {
        key: 'BOOLEAN',
        name: 'Boolean',
        icon: null,
        description: null,
        position: 50
    },
    ENUM: {
        key: 'ENUM',
        name: 'Enum',
        icon: null,
        description: null,
        position: 60
    },
    UNSPECIFIED: {
        key: 'UNSPECIFIED',
        name: 'Unspecified',
        icon: null,
        description: null,
        position: 70
    }
};


export const physicalSpecDefinitionType = {
    DELIMITED: {
        key: 'DELIMITED',
        name: 'Delimited',
        icon: null,
        description: null,
        position: 10
    }
};


export const relationshipKind = {
    HAS: {
        key: 'HAS',
        name: 'Has',
        icon: null,
        description: null,
        position: 10
    },
    DEPRECATES: {
        key: 'DEPRECATES',
        name: 'Deprecates',
        icon: null,
        description: null,
        position: 20
    },
    LOOSELY_RELATES_TO: {
        key: 'LOOSELY_RELATES_TO',
        name: 'Loosely Relates To',
        icon: null,
        description: null,
        position: 30
    },
    PARTICIPATES_IN: {
        key: 'PARTICIPATES_IN',
        name: 'Participates In',
        icon: null,
        description: null,
        position: 40
    },
    RELATES_TO: {
        key: 'RELATES_TO',
        name: 'Relates To',
        icon: null,
        description: null,
        position: 50
    },
    SUPPORTS: {
        key: 'SUPPORTS',
        name: 'Supports',
        icon: null,
        description: null,
        position: 60
    },
    APPLICATION_NEW: {
        key: 'APPLICATION_NEW',
        name: 'Application - new',
        icon: null,
        description: null,
        position: 70
    },
    APPLICATION_FUNCTIONAL_CHANGE: {
        key: 'APPLICATION_FUNCTIONAL_CHANGE',
        name: 'Application - functional change',
        icon: null,
        description: null,
        position: 80
    },
    APPLICATION_DECOMMISSIONED: {
        key: 'APPLICATION_DECOMMISSIONED',
        name: 'Application - decommissioned',
        icon: null,
        description: null,
        position: 90
    },
    APPLICATION_NFR_CHANGE: {
        key: 'APPLICATION_NFR_CHANGE',
        name: 'Application - NFR change',
        icon: null,
        description: null,
        position: 100
    },
    DATA_PUBLISHER: {
        key: 'DATA_PUBLISHER',
        name: 'Data publisher',
        icon: null,
        description: null,
        position: 110
    },
    DATA_CONSUMER: {
        key: 'DATA_CONSUMER',
        name: 'Data consumer',
        icon: null,
        description: null,
        position: 120
    }
};


export const surveyInstanceStatus = {
    COMPLETED: {
        key: 'COMPLETED',
        name: 'Completed',
        icon: null,
        description: null,
        position: 10
    },
    EXPIRED: {
        key: 'EXPIRED',
        name: 'Expired',
        icon: null,
        description: null,
        position: 20
    },
    IN_PROGRESS: {
        key: 'IN_PROGRESS',
        name: 'In Progress',
        icon: null,
        description: null,
        position: 30
    },
    NOT_STARTED: {
        key: 'NOT_STARTED',
        name: 'Not Started',
        icon: null,
        description: null,
        position: 40
    },
    WITHDRAWN: {
        key: 'WITHDRAWN',
        name: 'Withdrawn',
        icon: null,
        description: null,
        position: 50
    }
};


export const releaseLifecycleStatus = {
    DRAFT: {
        key: 'DRAFT',
        name: 'Draft',
        icon: null,
        description: null,
        position: 10
    },
    ACTIVE: {
        key: 'ACTIVE',
        name: 'Active',
        icon: null,
        description: null,
        position: 20
    },
    DEPRECATED: {
        key: 'DEPRECATED',
        name: 'Deprecated',
        icon: null,
        description: null,
        position: 30
    },
    OBSOLETE: {
        key: 'OBSOLETE',
        name: 'Obsolete',
        icon: null,
        description: null,
        position: 40
    }
};


export const surveyRunStatus = {
    DRAFT: {
        key: 'DRAFT',
        name: 'Draft',
        icon: null,
        description: null,
        position: 10
    },
    ISSUED: {
        key: 'ISSUED',
        name: 'Issued',
        icon: null,
        description: null,
        position: 20
    },
    COMPLETED: {
        key: 'COMPLETED',
        name: 'Completed',
        icon: null,
        description: null,
        position: 30
    }
};


export const surveyQuestionFieldType = {
    APPLICATION: {
        key: 'APPLICATION',
        name: 'Application',
        icon: null,
        description: null,
        position: 10
    },
    BOOLEAN: {
        key: 'BOOLEAN',
        name: 'Boolean',
        icon: null,
        description: null,
        position: 20
    },
    DATE: {
        key: 'DATE',
        name: 'Date',
        icon: null,
        description: null,
        position: 25
    },
    DROPDOWN: {
        key: 'DROPDOWN',
        name: 'Dropdown',
        icon: null,
        description: null,
        position: 30
    },
    NUMBER: {
        key: 'NUMBER',
        name: 'Number',
        icon: null,
        description: null,
        position: 40
    },
    PERSON: {
        key: 'PERSON',
        name: 'Person',
        icon: null,
        description: null,
        position: 50
    },
    TEXT: {
        key: 'TEXT',
        name: 'Text',
        icon: null,
        description: null,
        position: 60
    },
    TEXTAREA: {
        key: 'TEXTAREA',
        name: 'Text Area',
        icon: null,
        description: null,
        position: 70
    }
};


export const issuance = {
    GROUP: {
        key: 'GROUP',
        name: 'Group',
        icon: null,
        description: null,
        position: 10
    },
    INDIVIDUAL: {
        key: 'INDIVIDUAL',
        name: 'Individual',
        icon: null,
        description: null,
        position: 20
    }
};


export const hierarchyQueryScope = {
    EXACT: {
        key: 'EXACT',
        name: 'Exact',
        icon: null,
        description: null,
        position: 10

    },
    PARENTS: {
        key: 'PARENTS',
        name: 'Parents',
        icon: null,
        description: null,
        position: 30

    },
    CHILDREN: {
        key: 'CHILDREN',
        name: 'Children',
        icon: null,
        description: null,
        position: 20
    }
};


export const enums = {
    action,
    applicationKind,
    applicationRating,
    attestationType,
    BOOLEAN,
    capabilityRating,
    entityLifecycleStatus,
    investmentRating,
    lifecyclePhase,
    orgUnitKind,
    severity,
    entity,
    changeInitiative,
    entityStatistic,
    hierarchyQueryScope,
    usageKind,
    criticality,
    rollupKind,
    endOfLifeStatus,
    transportKind,
    frequencyKind,
    dataFormatKind,
    lifecycleStatus,
    physicalSpecDefinitionFieldType,
    physicalSpecDefinitionType,
    rag,
    relationshipKind,
    surveyInstanceStatus,
    releaseLifecycleStatus,
    surveyRunStatus,
    surveyQuestionFieldType,
    issuance
};


export function getEnumName(enumValues = {}, key) {
    return enumValues[key] ? enumValues[key].name : key;
}


/**
 * Used to convert a map of ( { code -> displayName }
 * @param lookups
 * @param excludeUnknown
 */
export function toOptions(lookups = {}, excludeUnknown = false) {
    return _.chain(lookups)
        .map((v, k) => ({name: v.name, code: k, position: v.position}))
        .sortBy(['position', 'name'])
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
        (v, k) => ({label: v.name, value: k}));
}
