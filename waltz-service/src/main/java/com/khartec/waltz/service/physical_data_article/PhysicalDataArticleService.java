package com.khartec.waltz.service.physical_data_article;

import com.khartec.waltz.data.physical_data_article.PhysicalDataArticleDao;
import com.khartec.waltz.data.physical_data_article.PhysicalDataArticleSelectorFactory;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.ProduceConsumeGroup;
import com.khartec.waltz.model.physical_data_article.PhysicalDataArticle;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class PhysicalDataArticleService {

    private final PhysicalDataArticleDao dataArticleDao;
    private final PhysicalDataArticleSelectorFactory selectorFactory;


    @Autowired
    public PhysicalDataArticleService(PhysicalDataArticleDao dataArticleDao, 
                                      PhysicalDataArticleSelectorFactory selectorFactory) 
    {
        checkNotNull(dataArticleDao, "dataArticleDao cannot be null");
        checkNotNull(selectorFactory, "selectorFactory cannot be null");
        this.dataArticleDao = dataArticleDao;
        this.selectorFactory = selectorFactory;
    }


    public ProduceConsumeGroup<PhysicalDataArticle> findByAppId(long id) {
        return dataArticleDao.findByAppId(id);
    }


    public List<PhysicalDataArticle> findByProducerAppId(long id) {
        return dataArticleDao.findByProducerAppId(id);
    }


    public Collection<PhysicalDataArticle> findByConsumerAppId(long id) {
        return dataArticleDao.findByConsumerAppId(id);
    }


    public PhysicalDataArticle getById(long id) {
        return dataArticleDao.getById(id);
    }

    public Collection<PhysicalDataArticle> findBySelector(IdSelectionOptions options) {
        Select<Record1<Long>> selector = selectorFactory.apply(options);
        return dataArticleDao.findBySelector(selector);

    }
}
