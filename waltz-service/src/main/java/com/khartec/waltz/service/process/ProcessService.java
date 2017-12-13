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

package com.khartec.waltz.service.process;

import com.khartec.waltz.data.process.ProcessDao;
import com.khartec.waltz.model.process.Process;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class ProcessService {
    
    private final ProcessDao processDao;
    private final DSLContext dsl;


    @Autowired
    public ProcessService(ProcessDao processDao,
                          DSLContext dsl) {
        checkNotNull(processDao, "processDao cannot be null");
        checkNotNull(dsl, "dsl cannot be null");

        this.processDao = processDao;
        this.dsl = dsl;
    }


    public Process getById(long id) {
        return processDao.getById(id);
    }


    public List<Process> findAll() {
        return processDao.findAll();
    }


    public Collection<Process> findForApplication(long id) {
        return processDao.findForApplication(id);
    }
}
