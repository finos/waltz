package org.finos.waltz.jobs.example.demodata;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.*;
import org.jooq.DSLContext;

import java.util.Map;

import static org.finos.waltz.schema.Tables.ALLOCATION;
import static org.finos.waltz.schema.Tables.ALLOCATION_SCHEME;

public class DemoUtils {


    public static final OrganisationalUnit ou = Tables.ORGANISATIONAL_UNIT;
    public static final MeasurableCategory mc = Tables.MEASURABLE_CATEGORY;
    public static final RatingScheme rs = Tables.RATING_SCHEME;
    public static final RatingSchemeItem rsi = Tables.RATING_SCHEME_ITEM;
    public static final Measurable m = Tables.MEASURABLE;
    public static final MeasurableRating mr = Tables.MEASURABLE_RATING;
    public static final Application app = Tables.APPLICATION;
    public static final DataType dt = Tables.DATA_TYPE;
    public static final LogicalFlow lf = Tables.LOGICAL_FLOW;
    public static final LogicalFlowDecorator lfd = Tables.LOGICAL_FLOW_DECORATOR;
    public static final AssessmentRating ar = Tables.ASSESSMENT_RATING;
    public static final AssessmentDefinition ad = Tables.ASSESSMENT_DEFINITION;
    public static final AllocationScheme allocScheme = ALLOCATION_SCHEME;
    public static final Allocation alloc = ALLOCATION;


    public static final String PROVENANCE = "test";
    public static final String USER_ID = "admin";

    public static String strVal(Row row,
                                 int col) {
        Cell cell = row.getCell(col);
        return cell == null ? null : cell.getStringCellValue();
    }


    public static Double numberVal(Row row,
                                 int col) {
        Cell cell = row.getCell(col);
        return cell == null ? null : cell.getNumericCellValue();
    }


    public static Map<String, Long> fetchCapabilityExtIdToIdMap(DSLContext waltz) {
        return waltz
                .select(m.EXTERNAL_ID, m.ID)
                .from(m)
                .fetchMap(m.EXTERNAL_ID, m.ID);
    }


    public static Map<String, Long> fetchAppExtIdToIdMap(DSLContext waltz) {
        return waltz
                .select(app.ASSET_CODE, app.ID)
                .from(app)
                .fetchMap(app.ASSET_CODE, app.ID);
    }


    public static Map<String, Long> fetchDataTypeExtIdToIdMap(DSLContext waltz) {
        return waltz
                .select(dt.CODE, dt.ID)
                .from(dt)
                .fetchMap(dt.CODE, dt.ID);
    }


    public static String toExtId(String str) {
        return str
                .toLowerCase()
                .replace(" ", "_")
                .replace("\"", "")
                .replace("\'", "")
                .replace("(", "")
                .replace(")", "")
                .replace("\t", " ");
    }

}
