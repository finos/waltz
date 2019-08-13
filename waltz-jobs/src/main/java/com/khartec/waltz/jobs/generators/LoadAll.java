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

package com.khartec.waltz.jobs.generators;

import com.khartec.waltz.common.LoggingUtilities;
import com.khartec.waltz.service.DIConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;
import java.util.Objects;


public class LoadAll {

    private static final boolean SKIP_SLOW = false;

    private static final SampleDataGenerator[] loaders = new SampleDataGenerator[] {
            new DemoSettingsGenerator(),
//            new DataTypeGenerator(),
//            new OrgUnitGenerator(),
//            SKIP_SLOW ? null : new PersonDataGenerator(),
//            new AppGenerator(),
//            new AppGroupGenerator(),
//            new AppGroupEntryGenerator(),
//            new BookmarkGenerator(),
//            new ChangeInitiativeGenerator(),
//            new ProcessGenerator(),
//            new MeasurableGenerator("PRODUCT"),
//            new MeasurableGenerator("CAPABILITY"),
//            new MeasurableGenerator("REGULATION"),
//            new MeasurableRatingGenerator(),
//            SKIP_SLOW ? null : new EntityStatisticGenerator(),
//            new AuthSourceGenerator(),
//            new AssessmentGenerator(),
//            new RoadmapGenerator(),
            SKIP_SLOW ? null : new ServerGenerator(),
//            new LogicalFlowGenerator(),
//            new LogicalFlowDecorationGenerator(),
//            new PhysicalSpecificationGenerator(),
//            new PhysicalFlowGenerator(),
//            new PhysicalFlowParticipantGenerator(),
//            new InvolvementGenerator(),
//            new DatabaseGenerator(),
//            new AssetCostGenerator(),
//            new ChangeLogGenerator(),
//            new EndUserAppGenerator(),
//            new EndUserAppInvolvmentGenerator(),
//            new SurveyTemplateGenerator(),
//            new SurveyRunGenerator(),
//            new ChangeSetGenerator(),
//            new ChangeUnitGenerator()
    };


    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        LoggingUtilities.configureLogging();

        Arrays.stream(loaders)
                .filter(Objects::nonNull)
                .forEach(loader -> {
                    log("Starting loader: %s", loader.getClass().getSimpleName());
                    log("Cleanup");
                    loader.remove(ctx);
                    log("Generate");
                    loader.create(ctx);
                    log("Done");
                });
    }

    private static void log(String s, Object... args) {
        System.out.println(String.format(s, args));
    }
}
