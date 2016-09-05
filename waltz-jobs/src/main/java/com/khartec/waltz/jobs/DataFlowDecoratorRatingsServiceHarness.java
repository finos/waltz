package com.khartec.waltz.jobs;

import com.khartec.waltz.model.authoritativesource.AuthoritativeSource;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.authoritative_source.AuthoritativeSourceService;
import com.khartec.waltz.service.data_flow_decorator.DataFlowDecoratorRatingsService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by dwatkins on 03/09/2016.
 */
public class DataFlowDecoratorRatingsServiceHarness {

    public static void main(String[] args) throws SQLException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DataFlowDecoratorRatingsService ratingService = ctx.getBean(DataFlowDecoratorRatingsService.class);
        AuthoritativeSourceService authoritativeSourceService = ctx.getBean(AuthoritativeSourceService.class);

        List<AuthoritativeSource> authSources = authoritativeSourceService.findAll();

        for (AuthoritativeSource authSource: authSources) {
            ratingService.updateRatingsForAuthSource(authSource.dataType(), authSource.parentReference());
        }

    }

}
