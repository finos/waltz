package com.khartec.waltz.service.static_panel;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.data.static_panel.StaticPanelDao;
import com.khartec.waltz.model.staticpanel.ContentKind;
import com.khartec.waltz.model.staticpanel.ImmutableStaticPanel;
import com.khartec.waltz.model.staticpanel.StaticPanel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class StaticPanelService {


    private final StaticPanelDao staticPanelDao;


    @Autowired
    public StaticPanelService(StaticPanelDao staticPanelDao) {
        this.staticPanelDao = staticPanelDao;
    }

    public List<StaticPanel> findByGroup(String group) {
        return staticPanelDao.findByGroup(group);
    }
}
