package com.khartec.waltz.jobs.tools.exporters.archi;

import com.khartec.waltz.job.tools.exporters.archi.PropertyDefinitionType;

import static com.khartec.waltz.jobs.tools.exporters.archi.ArchiUtilities.mkStrPropDef;

public interface WaltzArchiProperties {
    PropertyDefinitionType NAME = mkStrPropDef("waltz-name", "name");
    PropertyDefinitionType EXTERNAL_ID = mkStrPropDef("waltz-external-id", "external-id");
    PropertyDefinitionType APP_TYPE = mkStrPropDef("waltz-app-type", "app-type");
    PropertyDefinitionType APP_CRITICALITY = mkStrPropDef("waltz-app-criticality", "app-criticality");
    PropertyDefinitionType LIFECYCLE_PHASE = mkStrPropDef("waltz-lifecycle-phase", "lifecycle-phase");
}
