package com.khartec.waltz.service.custom_environment;

import com.khartec.waltz.data.custom_environment.CustomEnvironmentDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.custom_environment.CustomEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

@Service
public class CustomEnvironmentService {

    private final CustomEnvironmentDao customEnvironmentDao;

    @Autowired
    public CustomEnvironmentService(CustomEnvironmentDao customEnvironmentDao){
        this.customEnvironmentDao = customEnvironmentDao;
    }


    public Set<CustomEnvironment> findAll(){
        return customEnvironmentDao.findAll();
    }


    public Collection<CustomEnvironment> findByOwningEntityRef(EntityReference ref) {
        return customEnvironmentDao.findByOwningEntityRef(ref);
    }


    public Boolean create(CustomEnvironment env, String username) {
        //change logs for creating new env
        return customEnvironmentDao.create(env);
    }


    public Boolean remove(Long envId, String username) {
        //maybe front end warn that if this env is to be deleted will cascade and remove all usages
        //change logs for deleting env (+ usages?)
        return customEnvironmentDao.remove(envId);
    }


}

