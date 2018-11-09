package com.khartec.waltz.web;

import com.khartec.waltz.model.command.Command;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public class ModelArchitectureCompliance extends BaseArchitectureComplianceCheck {

    @Test
    public void commandsNeedMarkerInterface() {
        ArchRule rule = classes().that()
                .areNotInterfaces()
                .and()
                .haveNameMatching(".*Command")
                .should()
                .implement(Command.class);
        rule.check(waltzOnlyClasses);
    }

}
