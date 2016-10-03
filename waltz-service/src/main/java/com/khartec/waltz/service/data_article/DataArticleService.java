package com.khartec.waltz.service.data_article;

import com.khartec.waltz.data.data_article.DataArticleDao;
import com.khartec.waltz.model.data_article.DataArticle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class DataArticleService {

    private final DataArticleDao dataArticleDao;


    @Autowired
    public DataArticleService(DataArticleDao dataArticleDao) {
        checkNotNull(dataArticleDao, "dataArticleDao cannot be null");
        this.dataArticleDao = dataArticleDao;
    }


    public List<DataArticle> findForAppId(long id) {
        return dataArticleDao.findForAppId(id);
    }

}
