package com.khartec.waltz.jobs.generators;

import com.khartec.waltz.service.DIConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;
import java.util.Objects;


public class LoadAll {

    private static final boolean SKIP_SLOW = false;

    private static final SampleDataGenerator loaders[] = new SampleDataGenerator[] {
            new DataTypeGenerator(),
            new OrgUnitGenerator(),
            SKIP_SLOW ? null : new PersonDataGenerator(),
            new AppGenerator(),
            new AppGroupGenerator(),
            new AppGroupEntryGenerator(),
            new BookmarkGenerator(),
            new ChangeInitiativeGenerator(),
            new ProcessGenerator(),
            new MeasurableGenerator("PRODUCT"),
            new MeasurableGenerator("CAPABILITY"),
            new MeasurableGenerator("REGULATION"),
            new MeasurableRatingGenerator(),
            SKIP_SLOW ? null : new EntityStatisticGenerator(),
            new AuthSourceGenerator(),
            new ServerGenerator(),
            new LogicalFlowGenerator(),
            new LogicalFlowDecorationGenerator(),
            new PhysicalSpecificationGenerator(),
            new PhysicalFlowGenerator(),
            new PhysicalFlowParticipantGenerator(),
            new InvolvementGenerator(),
            new DatabaseGenerator(),
            new AssetCostGenerator(),
            new ChangeLogGenerator(),
            new EndUserAppGenerator(),
            new EndUserAppInvolvmentGenerator(),
            new SurveyTemplateGenerator(),
            new SurveyRunGenerator(),
    };


    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

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
