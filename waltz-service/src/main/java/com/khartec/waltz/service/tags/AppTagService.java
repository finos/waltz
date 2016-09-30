package com.khartec.waltz.service.tags;

import com.khartec.waltz.data.application.AppTagDao;
import com.khartec.waltz.model.application.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

/**
 * Tags for applications
 */
@Service
public class AppTagService {

    private final AppTagDao appTagDao;


    @Autowired
    public AppTagService(AppTagDao appTagDao) {
        this.appTagDao = appTagDao;
    }


    public List<String> findAllTags() {
        return appTagDao.findAllTags();
    }


    public List<String> findTagsForApplication(long appId) {
        return appTagDao.findTagsForApplication(appId);
    }


    public List<Application> findByTag(String tag) {
        return appTagDao.findByTag(tag);
    }


    public int[] updateTags(long id, Collection<String> tags) {
        checkNotNull(tags, "tags cannot be null");
        return appTagDao.updateTags(id, tags);
    }
}
