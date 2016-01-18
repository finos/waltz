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

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.web.endpoints.Endpoint;
import com.khartec.waltz.web.ServerMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static spark.SparkBase.externalStaticFileLocation;
import static spark.SparkBase.staticFileLocation;

public class StaticResourcesEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(StaticResourcesEndpoint.class);

    private final ServerMode mode;


    public StaticResourcesEndpoint(ServerMode mode) {
        checkNotNull(mode, "Mode cannot be null");
        this.mode = mode;
    }


    @Override
    public void register() {
        LOG.debug("Registering static resources for mode {}", mode);

        if (mode == ServerMode.DEPLOY) {
            staticFileLocation("/static");
        } else if (mode == ServerMode.DEV) {
            String path = new File("waltz-ng/dist").getAbsolutePath();
            LOG.debug("Will be looking for static resources in {}", path);
            System.out.println("Will be looking for static resources in " + path);
            externalStaticFileLocation(path);
        }
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StaticResourcesEndpoint{");
        sb.append("mode=").append(mode);
        sb.append('}');
        return sb.toString();
    }
}
