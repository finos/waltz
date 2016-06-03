package com.khartec.waltz.service.process;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.process.ProcessDao;
import com.khartec.waltz.model.process.Process;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProcessService {
    
    private final ProcessDao processDao;


    @Autowired
    public ProcessService(ProcessDao processDao) {
        Checks.checkNotNull(processDao, "processDao cannot be null");
        this.processDao = processDao;
    }


    public Process getById(long id) {
        return processDao.getById(id);
    }


    public List<Process> findAll() {
        return processDao.findAll();
    }

}
