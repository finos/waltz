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
}
