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

package com.khartec.waltz.service.enum_value;


import com.khartec.waltz.data.enum_value.EnumValueDao;
import com.khartec.waltz.model.EnumValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class EnumValueService {

    private final EnumValueDao enumValueDao;


    @Autowired
    public EnumValueService(EnumValueDao enumValueDao) {
        checkNotNull(enumValueDao, "enumValueDao cannot be null");
        this.enumValueDao = enumValueDao;
    }


    public List<EnumValue> findAll() {
        return enumValueDao.findAll();
    }
}
