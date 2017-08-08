/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

package com.khartec.waltz.service.attestation;


import com.khartec.waltz.data.attestation.AttestationDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.attestation.Attestation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class AttestationService {

    private final AttestationDao attestationDao;


    @Autowired
    public AttestationService(AttestationDao attestationDao) {
        checkNotNull(attestationDao, "attestationDao cannot be null");

        this.attestationDao = attestationDao;
    }


    public List<Attestation> findForEntity(EntityReference entityReference) {
        return attestationDao.findForEntity(entityReference);
    }
}
