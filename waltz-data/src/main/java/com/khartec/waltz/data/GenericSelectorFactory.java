package com.khartec.waltz.data;

import com.khartec.waltz.data.measurable.MeasurableIdSelectorFactory;
import com.khartec.waltz.data.orgunit.OrganisationalUnitIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenericSelectorFactory {

        private final MeasurableIdSelectorFactory measurableIdSelectorFactory;
        private final OrganisationalUnitIdSelectorFactory organisationalUnitIdSelectorFactory;


        @Autowired
        public GenericSelectorFactory(MeasurableIdSelectorFactory measurableIdSelectorFactory, OrganisationalUnitIdSelectorFactory organisationalUnitIdSelectorFactory) {
            this.measurableIdSelectorFactory = measurableIdSelectorFactory;
            this.organisationalUnitIdSelectorFactory = organisationalUnitIdSelectorFactory;
        }


        public GenericSelector apply(IdSelectionOptions selectionOptions) {
            EntityKind kind = selectionOptions.entityReference().kind();

            ImmutableGenericSelector.Builder builder = ImmutableGenericSelector.builder()
                    .kind(kind);

            if (selectionOptions.scope() == HierarchyQueryScope.EXACT) {
                Select<Record1<Long>> select = DSL.select(DSL.val(selectionOptions.entityReference().id()));
                return builder
                        .selector(select)
                        .build();
            }
            switch (kind) {
                case MEASURABLE:
                    builder.selector(measurableIdSelectorFactory.apply(selectionOptions));
                    break;
                case ORG_UNIT:
                    builder.selector(organisationalUnitIdSelectorFactory.apply(selectionOptions));
                    break;
                default:
                    throw new UnsupportedOperationException(String.format("Cannot make generic selector for kind: %s", kind));
            }

            return builder.build();
        }
    }