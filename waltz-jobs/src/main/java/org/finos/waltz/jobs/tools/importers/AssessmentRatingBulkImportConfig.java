package org.finos.waltz.jobs.tools.importers;

import org.immutables.value.Value;

@Value.Immutable
public abstract class AssessmentRatingBulkImportConfig {

    public abstract Long assessmentDefinitionId();

    @Value.Default
    public Integer numberOfHeaderRows(){
        return 1;
    };

    @Value.Default
    public Integer sheetPosition(){
        return 0;
    }

    @Value.Default
    public  String updateUser(){
        return "assessment_rating_bulk_import";
    };

    @Value.Default
    public SynchronisationMode mode(){
        return SynchronisationMode.DELTA;
    }

}
