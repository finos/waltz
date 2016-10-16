package com.khartec.waltz.jobs;

import com.khartec.waltz.model.lineage_report.LineageReportDescriptor;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.lineage_report.LineageReportService;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

/**
 * Created by dwatkins on 16/10/2016.
 */
public class LineageHarness {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);


        LineageReportService service = ctx.getBean(LineageReportService.class);


        List<LineageReportDescriptor> reports = service.findReportsContributedToByArticle(1484);

        reports.forEach(r -> System.out.println(r));

    }
}
