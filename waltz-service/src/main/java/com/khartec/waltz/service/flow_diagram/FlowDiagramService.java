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

package com.khartec.waltz.service.flow_diagram;

import com.khartec.waltz.data.flow_diagram.FlowDiagramDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.flow_diagram.FlowDiagram;
import com.khartec.waltz.model.flow_diagram.SaveDiagramCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class FlowDiagramService {

    private final FlowDiagramDao flowDiagramDao;


    @Autowired
    public FlowDiagramService(FlowDiagramDao flowDiagramDao) {
        checkNotNull(flowDiagramDao, "flowDiagramDao cannot be null");
        this.flowDiagramDao = flowDiagramDao;
    }


    public FlowDiagram getById(long diagramId) {
        return flowDiagramDao.getById(diagramId);
    }


    public List<FlowDiagram> findByEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return flowDiagramDao.findByEntityReference(ref);
    }


    public long save(SaveDiagramCommand saveDiagramCommand, String username) {
        checkNotNull(saveDiagramCommand, "saveDiagramCommand cannot be null");
        checkNotNull(username, "username cannot be null");

        System.out.println("Saving "+saveDiagramCommand + " for " + username);

        return 1;
    }

}
