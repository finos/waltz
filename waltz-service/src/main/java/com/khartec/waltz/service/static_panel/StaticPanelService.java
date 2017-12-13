/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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
