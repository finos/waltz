package com.khartec.waltz.model.enum_value;

public enum EnumValueKind {
    TRANSPORT_KIND("TransportKind"),
    BOOKMARK_KIND("BookmarkKind"),
    AUTHORITATIVENESS_RATING("AuthoritativenessRating"),
    COST_KIND("CostKind"),
    PERSON_KIND("PersonKind"),
    SCENARIO_TYPE("ScenarioType"),
    CHANGE_INITIATIVE_LIFECYCLE_PHASE("changeInitiativeLifecyclePhase"),
    PHYSICAL_FLOW_CRITICALITY("physicalFlowCriticality"),
    FRESHNESS_INDICATOR("FreshnessIndicator");

    private final String dbValue;


    EnumValueKind(String dbValue) {
        this.dbValue = dbValue;
    }


    public String dbValue() {
        return dbValue;
    }
}
