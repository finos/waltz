/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.service.static_panel;

import com.khartec.waltz.common.ArrayUtilities;
import com.khartec.waltz.data.static_panel.StaticPanelDao;
import com.khartec.waltz.model.staticpanel.StaticPanel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;


@Service
public class StaticPanelService {


    private final StaticPanelDao staticPanelDao;


    @Autowired
    public StaticPanelService(StaticPanelDao staticPanelDao) {
        this.staticPanelDao = staticPanelDao;
    }


    public List<StaticPanel> findByGroups(String[] groups) {
        if (ArrayUtilities.isEmpty(groups)) return Collections.emptyList();
        return staticPanelDao.findByGroups(groups);
    }


    public List<StaticPanel> findAll() {
        return staticPanelDao.findAll();
    }


    public boolean save(StaticPanel panel) {
        return panel.id().isPresent()
                ? staticPanelDao.update(panel)
                : staticPanelDao.create(panel);
    }

}
