/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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

package com.khartec.waltz.common;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.util.List;
import java.util.stream.Stream;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static java.util.stream.Collectors.toList;


public class IOUtilities {

    public static List<String> readLines(InputStream stream) {
        checkNotNull(stream, "stream must not be null");
        return streamLines(stream).collect(toList());
    }


    public static Stream<String> streamLines(InputStream inputStream) {
        checkNotNull(inputStream, "inputStream must not be null");
        InputStreamReader streamReader = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(streamReader);
        return reader
                .lines();
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


    public static Resource getFileResource(String fileName) {
        Resource resource = new ClassPathResource(fileName);
        if (!resource.exists()) {
            String home = System.getProperty("user.home");
            resource = new FileSystemResource(home + "/.waltz/" + fileName);
        }
        return resource;
    }
}
