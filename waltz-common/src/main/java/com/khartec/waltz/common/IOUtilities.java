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

import java.io.*;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static java.util.stream.Collectors.toList;


public class IOUtilities {

    public static List<String> readLines(InputStream stream) throws IOException {
        checkNotNull(stream, "stream must not be null");

        try {
            InputStreamReader streamReader = new InputStreamReader(stream);
            BufferedReader reader = new BufferedReader(streamReader);
            return reader
                    .lines()
                    .collect(toList());
        } finally {
            stream.close();
        }
    }


    public static void copyStream(InputStream input, OutputStream output)
            throws IOException
    {
        checkNotNull(input, "Input stream cannot be null");
        checkNotNull(input, "Output stream cannot be null");

        byte[] buff = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buff)) != -1) {
            output.write(buff, 0, bytesRead);
        }
    }
}
