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

package com.khartec.waltz.jobs;

import com.khartec.waltz.schema.tables.records.PersonRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;
import org.jooq.Loader;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultExecuteListener;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.jooq.tools.json.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.khartec.waltz.schema.Tables.PERSON;


public class MSSqlImportExportExample {

    public static void main(String[] args) throws ParseException, IOException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        if (false) {
            System.out.println("-- saving");
            dsl.selectFrom(PERSON)
                    .fetch()
                    .formatCSV(new FileOutputStream("person.csv"), ',', "{null}");
        }

        if (true) {
            System.out.println("-- deleting");
            dsl.deleteFrom(PERSON).execute();

            dsl.transaction(cfg -> {
                // insert the identity insert statement
                ExecuteListener listener = new DefaultExecuteListener() {
                    @Override
                    public void renderEnd(ExecuteContext ctx) {
                        ctx.sql("SET IDENTITY_INSERT [person] ON " + ctx.sql());
                    }
                };
                cfg.set(new DefaultExecuteListenerProvider(listener));

                DSLContext tx = DSL.using(cfg);

                System.out.println("-- loading");
                Loader<PersonRecord> loader = tx
                        .loadInto(PERSON)
                        .loadCSV(new FileInputStream("person.csv"))
                        .fields(PERSON.fields())
                        .nullString("{null}") //treat this from the csv as a database NULL value
                        .execute();


                System.out.println("processed:" + loader.processed());
                System.out.println("stored:" + loader.stored());
                System.out.println("ignored:" + loader.ignored());
                loader.errors().forEach(e -> System.out.println("error:" + e.exception().getMessage()));
            });
        }

        System.out.println("-- done");
    }

}
