/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.jobs;

import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.application.AssetCodeRelationshipKind;
import com.khartec.waltz.model.trait.ImmutableTrait;

import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.ListUtilities.map;


public class AppHarness {

    public static void main(String[] args) {

//        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
//        ApplicationService applicationService = ctx.getBean(ApplicationService.class);
//        DSLContext dsl = ctx.getBean(DSLContext.class);
//
//        List<String> tagList = applicationService.findAllTags();
//
//        tagList.forEach(System.out::println);
//
//        System.out.println("---------------");
//
//        applicationService.findByTag("not-good-at-flying").forEach(a -> System.out.println(a.name()));
//
//        System.out.println(applicationService.findTagsForApplication(521L));
//

        ImmutableTrait t = ImmutableTrait.builder()
                .description("HEllo")
                .icon("Hello")
                .name("fdjlkfjd;")
                .applicationDeclarable(true)
                .build();

        System.out.println(t);
    }


    private static void prettyPrint(Map<AssetCodeRelationshipKind, List<Application>> grouped) {
        grouped.forEach((key, apps) ->
                System.out.println(key.name() + map(apps, relatedApp -> "\n\t"+ toString(relatedApp))));
    }


    private static String toString(Application app) {
        return app.name() + " " + app.assetCode() + " / " + app.parentAssetCode();
    }

}
