package com.khartec.waltz.data.static_panel;

import com.khartec.waltz.model.staticpanel.ContentKind;
import com.khartec.waltz.model.staticpanel.ImmutableStaticPanel;
import com.khartec.waltz.model.staticpanel.StaticPanel;
import com.khartec.waltz.schema.tables.records.StaticPanelRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.schema.tables.StaticPanel.STATIC_PANEL;

@Repository
public class StaticPanelDao {

    private final DSLContext dsl;
    private RecordMapper<Record, StaticPanel> panelMapper = r -> {
        StaticPanelRecord record = r.into(STATIC_PANEL);
        return ImmutableStaticPanel.builder()
                .id(record.getId())
                .content(record.getContent())
                .group(record.getGroup())
                .icon(record.getIcon())
                .kind(ContentKind.valueOf(record.getKind()))
                .title(record.getTitle())
                .priority(record.getPriority())
                .width(record.getWidth())
                .build();
    };


    @Autowired
    public StaticPanelDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<StaticPanel> findByGroup(String group) {
        return dsl.select(STATIC_PANEL.fields())
                .from(STATIC_PANEL)
                .where(STATIC_PANEL.GROUP.eq(group))
                .orderBy(STATIC_PANEL.PRIORITY.asc())
                .fetch(panelMapper);
    }
}
