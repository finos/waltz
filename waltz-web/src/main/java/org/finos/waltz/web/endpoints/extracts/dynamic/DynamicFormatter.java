package org.finos.waltz.web.endpoints.extracts.dynamic;

import org.finos.waltz.model.report_grid.ReportGrid;
import org.finos.waltz.model.report_grid.ReportGridColumnDefinition;
import org.finos.waltz.model.report_grid.ReportSubject;
import org.finos.waltz.web.endpoints.extracts.ExtractFormat;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface DynamicFormatter {

    byte[] format(String id,
                  ReportGrid reportGrid,
            List<Tuple2<ReportGridColumnDefinition, Boolean>> columnDefinitions,
                  List<Tuple2<ReportSubject, ArrayList<Object>>> reportRows)  throws IOException;
}
