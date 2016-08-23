/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service.datatype_source_sink;

import com.khartec.waltz.data.data_type.DataTypeIdSelectorFactory;
import com.khartec.waltz.data.datatype_source_sink.SourceSinkDao;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.datatype_source_sink.DataTypeSourceSinks;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class SourceSinkService {

    private DataTypeIdSelectorFactory dataTypeIdSelectorFactory;
    private final SourceSinkDao sourceSinkDao;


    @Autowired
    public SourceSinkService(DataTypeIdSelectorFactory dataTypeIdSelectorFactory,
                             SourceSinkDao sourceSinkDao) {
        checkNotNull(dataTypeIdSelectorFactory, "dataTypeIdSelectorFactory cannot be null");
        checkNotNull(sourceSinkDao, "sourceSinkDao must not be null");

        this.dataTypeIdSelectorFactory = dataTypeIdSelectorFactory;
        this.sourceSinkDao = sourceSinkDao;
    }


    public List<DataTypeSourceSinks> findByIdSelector(IdSelectionOptions dataTypeIdSelectionOptions) {
        Select<Record1<Long>> idSelect = dataTypeIdSelectorFactory.apply(dataTypeIdSelectionOptions);
        return sourceSinkDao.findByIdSelector(idSelect);
    }

}
