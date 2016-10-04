package com.khartec.waltz.service.physical_data_article;

import com.khartec.waltz.data.physical_data_article.PhysicalDataArticleDao;
import com.khartec.waltz.model.ProduceConsumeGroup;
import com.khartec.waltz.model.physical_data_article.PhysicalDataArticle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class PhysicalDataArticleService {

    private final PhysicalDataArticleDao dataArticleDao;


    @Autowired
    public PhysicalDataArticleService(PhysicalDataArticleDao dataArticleDao) {
        checkNotNull(dataArticleDao, "dataArticleDao cannot be null");
        this.dataArticleDao = dataArticleDao;
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
}
