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

package com.khartec.waltz.jobs;

import com.khartec.waltz.data.authoritative_source.AuthoritativeSourceDao;
import com.khartec.waltz.data.data_type.DataTypeIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.service.DIConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class AuthSourceHarness {




    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        AuthoritativeSourceDao dao = ctx.getBean(AuthoritativeSourceDao.class);
        DataTypeIdSelectorFactory dtSelectorFactory = ctx.getBean(DataTypeIdSelectorFactory.class);

        IdSelectionOptions dtSelectionOptions = IdSelectionOptions.mkOpts(
                EntityReference.mkRef(
                        EntityKind.DATA_TYPE,
                        5000),
                HierarchyQueryScope.EXACT);


        System.out.println(dao.calculateConsumersForDataTypeIdSelector(dtSelectorFactory.apply(dtSelectionOptions)));




    }



}
