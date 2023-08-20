/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.common;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.finos.waltz.common.Checks.checkNotNull;
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
        checkNotNull(output, "Output stream cannot be null");

        byte[] buff = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buff)) != -1) {
            output.write(buff, 0, bytesRead);
        }
    }


    public static String readAsString(InputStream stream) {
        checkNotNull(stream, "stream must not be null");

        return streamLines(stream)
                .collect(Collectors.joining("\n"));
    }


    /**
     * Attempts to locate <code>fileName</code> via either:
     * <ul>
     *     <li>root of classpath</li>
     *     <li>directory: <code>${user.home}/.waltz</code></li>
     * </ul>
     * @param fileName file (or path) to be located
     * @return Resource representing the file
     */
    public static Resource getFileResource(String fileName) {
        Resource resource = new ClassPathResource(fileName);
        if (!resource.exists()) {
            String home = System.getProperty("user.home");
            resource = new FileSystemResource(home + "/.waltz/" + fileName);
        }
        return resource;
    }
}
