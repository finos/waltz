package org.finos.waltz.jobs.tools.importers;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.InputStream;

import static org.finos.waltz.common.IOUtilities.readAsString;

public class ImporterExperiment {

    @Value.Immutable
    @JsonDeserialize(as = ImmutableMyEntity.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    interface MyEntity {
        String name();
        @JsonAlias({"external_id", "ext_id"})
        String externalId();
        @Nullable String description();
    }

    public static void main(String[] args) {

        try {
            go();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void go() throws IOException {
        System.out.println("-- JSON ----------------------");
        {
            ObjectMapper mapper = new ObjectMapper();
            InputStream jsonStream = ImporterExperiment.class.getClassLoader().getResourceAsStream("importer-experiment.json");
            MyEntity[] myEntities = mapper.readValue(readAsString(jsonStream), MyEntity[].class);
            for (MyEntity myEntity : myEntities) {
                System.out.println(myEntity);
            }
        }

        System.out.println("-- CSV ----------------------");
        {
            CsvSchema bootstrapSchema = CsvSchema.emptySchema().withHeader();

            ObjectMapper mapper = new CsvMapper();
            InputStream csvStream = ImporterExperiment.class.getClassLoader().getResourceAsStream("importer-experiment.csv");
            MappingIterator<MyEntity> myEntities = mapper.readerFor(MyEntity.class).with(bootstrapSchema).readValues(readAsString(csvStream));
            for (MyEntity myEntity : myEntities.readAll()) {
                System.out.println(myEntity);
            }
        }

        System.out.println("-- TSV ----------------------");
        {
            CsvSchema bootstrapSchema = CsvSchema.emptySchema().withHeader().withColumnSeparator('\t');

            ObjectMapper mapper = new CsvMapper();
            InputStream csvStream = ImporterExperiment.class.getClassLoader().getResourceAsStream("importer-experiment.tsv");
            MappingIterator<MyEntity> myEntities = mapper.readerFor(MyEntity.class).with(bootstrapSchema).readValues(readAsString(csvStream));
            for (MyEntity myEntity : myEntities.readAll()) {
                System.out.println(myEntity);
            }
        }
    }

}
