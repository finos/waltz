/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
