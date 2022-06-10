package org.finos.waltz.web.endpoints.extracts.dynamic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.finos.waltz.model.application.LifecyclePhase;
import org.finos.waltz.model.report_grid.ReportGridColumnDefinition;
import org.finos.waltz.model.report_grid.ReportSubject;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.finos.waltz.common.ListUtilities.*;
import static org.jooq.lambda.fi.util.function.CheckedConsumer.unchecked;

@Component
public class DynamicJSONFormatter implements DynamicFormatter {

    private static final Logger LOG = LoggerFactory.getLogger(DynamicJSONFormatter.class);
    private final FormatterUtils formatterUtils;

    public DynamicJSONFormatter(FormatterUtils formatterUtils){
        this.formatterUtils = formatterUtils;
    }


    @Override
    public byte[] format(String id,
                         List<Tuple2<ReportGridColumnDefinition, Boolean>> columnDefinitions,
                         List<Tuple2<ReportSubject, ArrayList<Object>>> reportRows)  throws IOException {
        try {
            LOG.info("Generating JSON data {}",id);
            return mkResponse(columnDefinitions,reportRows);
        } catch (IOException e) {
           LOG.warn("Encountered error when trying to generate JSON response.  Details:{}", e.getMessage());
           throw e;
        }
    }

    private byte[] mkResponse(List<Tuple2<ReportGridColumnDefinition, Boolean>> columnDefinitions,
                               List<Tuple2<ReportSubject, ArrayList<Object>>> reportRows) throws IOException {


//        List<String> headers = formatterUtils.mkHeaderStrings(columnDefinitions);
        StringWriter writer = new StringWriter();
//        CsvListWriter csvWriter = new CsvListWriter(writer, CsvPreference.EXCEL_PREFERENCE);
//
//        csvWriter.write(headers);
//        reportRows.forEach(unchecked(row -> csvWriter.write(simplify(row))));
//        csvWriter.flush();

        ImmutableChangeInitiativeSchema changeInitiativeSchema =
                ImmutableChangeInitiativeSchema.builder().id("id")
                        .name("dummy")
                        .phase("dummy-phase")
                        .build();
        LOG.info("Response {}", createMapper().writeValueAsString(changeInitiativeSchema));
        return createMapper()
                .writeValueAsBytes(changeInitiativeSchema);
    }


    private List<Object> simplify(Tuple2<ReportSubject, ArrayList<Object>> row) {

        long appId = row.v1.entityReference().id();
        String appName = row.v1.entityReference().name().get();
        Optional<String> assetCode = row.v1.entityReference().externalId();
        LifecyclePhase lifecyclePhase = row.v1.lifecyclePhase();

        List<Object> appInfo = asList(appId, appName, assetCode, lifecyclePhase.name());

        return map(concat(appInfo, row.v2), value -> {
            if (value == null) return null;
            if (value instanceof Optional) {
                return ((Optional<?>) value).orElse(null);
            } else {
                return value;
            }
        });
    }

    private ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // DateTime etc
        mapper.registerModule(new Jdk8Module()); // Optional etc
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }

}
