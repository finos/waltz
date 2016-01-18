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

package com.khartec.waltz.common;

import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static org.slf4j.LoggerFactory.getLogger;

public class HomeConfigFile {

    private static final Logger LOG = getLogger(HomeConfigFile.class);
    private final Properties props;


    public HomeConfigFile(String directoryName, String name) {
        Checks.checkNotNull(directoryName, "directoryName must not be null");
        Checks.checkNotEmptyString(name, "name must not be empty");

        String home = System.getProperty("user.home");

        File dir = new File(home, directoryName);
        Checks.checkTrue(dir.exists(), "Directory does not exist: " + dir.getAbsolutePath());

        File file = new File(dir, name);
        Checks.checkTrue(file.exists(), "Config file does not exist: " + file.getAbsolutePath());

        try {
            props = new Properties();
            props.load(new FileInputStream(file));
        } catch (IOException e) {
            String msg = String.format(
                    "Failed to load file %s, because: %s",
                    file.getAbsolutePath(),
                    e.getMessage());

            LOG.error(msg);
            throw new RuntimeException(msg, e);
        }
    }

    public String get(String key) {
        return props.getProperty(key);
    }

    public String get(String key, String dflt) {
        return props.getProperty(key, dflt);
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int dflt) {
        String strVal = props.getProperty(key);
        if (strVal == null) { return dflt; }
        try {
            return Integer.parseInt(strVal);
        } catch (Exception e) {
            LOG.warn("Could not parse integer from : "+strVal+" for key: "+key);
            return dflt;
        }
    }
}
