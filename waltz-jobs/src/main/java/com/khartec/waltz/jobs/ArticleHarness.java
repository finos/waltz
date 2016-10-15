package com.khartec.waltz.jobs;

import com.khartec.waltz.data.physical_data_article.PhysicalDataArticleDao;
import com.khartec.waltz.model.ProduceConsumeGroup;
import com.khartec.waltz.model.physical_data_article.PhysicalDataArticle;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Created by dwatkins on 13/05/2016.
 */
public class ArticleHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        PhysicalDataArticleDao physicalDataArticleDao = ctx.getBean(PhysicalDataArticleDao.class);

        ProduceConsumeGroup<PhysicalDataArticle> articles = physicalDataArticleDao.findByAppId(67502);
        System.out.println(articles);

    }


}
