package org.finos.waltz.data.assessment_rating;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.finos.waltz.model.assessment_definition.AssessmentRipplerJobConfiguration;
import org.finos.waltz.model.assessment_definition.AssessmentRipplerJobStep;
import org.junit.jupiter.api.Test;

import static org.finos.waltz.common.CollectionUtilities.first;
import static org.junit.jupiter.api.Assertions.*;

class AssessmentRatingRipplerTest {


    @Test
    public void testSettingsParse() throws JsonProcessingException {
        String value = "[ { \"from\" : \"FROM_DEF\", \"to\" : \"TO_DEF\" } ]";
        AssessmentRipplerJobConfiguration config = AssessmentRatingRippler.parseConfig("demoRippler", value);

        assertEquals("demoRippler", config.name());
        assertEquals(1, config.steps().size());

        AssessmentRipplerJobStep step = first(config.steps());
        assertEquals("FROM_DEF", step.fromDef());
        assertEquals("TO_DEF", step.toDef());
    }

}