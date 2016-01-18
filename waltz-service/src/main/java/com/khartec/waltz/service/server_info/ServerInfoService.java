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

package com.khartec.waltz.service.server_info;

import com.khartec.waltz.data.server_info.ServerInfoDao;
import com.khartec.waltz.model.serverinfo.ServerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class ServerInfoService {

    private final ServerInfoDao serverInfoDao;


    @Autowired
    public ServerInfoService(ServerInfoDao serverInfoDao) {
        checkNotNull(serverInfoDao, "serverInfoDao must not be null");
        
        this.serverInfoDao = serverInfoDao;
    }

    public List<ServerInfo> findByAssetCode(String assetCode) {
        return serverInfoDao.findByAssetCode(assetCode);
    }


    public List<ServerInfo> findByAppId(long appId) {
        return serverInfoDao.findByAppId(appId);
    }

}
