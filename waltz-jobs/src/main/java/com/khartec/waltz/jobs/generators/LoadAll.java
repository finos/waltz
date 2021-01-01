/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.jobs.generators;

import com.khartec.waltz.common.LoggingUtilities;
import com.khartec.waltz.service.DIConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class LoadAll {

    private static final boolean SKIP_SLOW = false;

    private static final HashMap<String, SampleDataGenerator> generators = new HashMap<String, SampleDataGenerator>() {{
        put( "DemoSettings", new DemoSettingsGenerator());
        put( "DataType", new DataTypeGenerator());
        put("OrgUnit", new OrgUnitGenerator());
        put( "PersonData", SKIP_SLOW ? null : new PersonDataGenerator());
        put( "App", new AppGenerator());
        put( "AppGroup", new AppGroupGenerator());
        put( "AppGroupEntry",  new AppGroupEntryGenerator());
        put( "Bookmark",  new BookmarkGenerator());
        put( "ChangeInitiative",  new ChangeInitiativeGenerator());
        put( "Process",  new ProcessGenerator());
        put( "Product",  new MeasurableGenerator("PRODUCT"));
        put( "Capability",  new MeasurableGenerator("CAPABILITY"));
        put( "Regulation",  new MeasurableGenerator("REGULATION"));
        put( "MeasurableRating",  new MeasurableRatingGenerator());
        put( "EntityStatistic",  SKIP_SLOW ? null : new EntityStatisticGenerator());
        put( "AuthSource",  new AuthSourceGenerator());
        put( "Assessment",  new AssessmentGenerator());
        put( "Roadmap",  new RoadmapGenerator());
        put( "Server",  SKIP_SLOW ? null : new ServerGenerator());
        put( "LogicalFlow",  new LogicalFlowGenerator());
        put( "LogicalFlowDecoration",  new LogicalFlowDecorationGenerator());
        put( "hysicalSpecification",  new PhysicalSpecificationGenerator());
        put( "PhysicalFlow",  new PhysicalFlowGenerator());
        put( "hysicalFlowParticipant",  new PhysicalFlowParticipantGenerator());
        put( "Involvement",  new InvolvementGenerator());
        put( "Database",  new DatabaseGenerator());
        put( "AssetCost",  new AssetCostGenerator());
        put( "ChangeLog",  new ChangeLogGenerator());
        put( "EndUserApp",  new EndUserAppGenerator());
        put( "EndUserAppInvolvment",  new EndUserAppInvolvmentGenerator());
        put( "SurveyTemplate",  new SurveyTemplateGenerator());
        put( "SurveyRun",  new SurveyRunGenerator());
        put( "ChangeSet",  new ChangeSetGenerator());
        put( "Licence",  new LicenceGenerator());
        put( "ChangeUnit",  new ChangeUnitGenerator());
    }};


    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        LoggingUtilities.configureLogging();


        ArrayList<SampleDataGenerator> loaders = new ArrayList<SampleDataGenerator>();

        if(args.length > 0) {
            for (String arg : args) {
                log("Data to be loaded : %s ", arg);
                loaders.add(generators.get(arg));
            }
        }
        else {
            log("ALL Data to be loaded");
            loaders.addAll(generators.values());
        }

        loaders.stream()
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
