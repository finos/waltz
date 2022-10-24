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

package org.finos.waltz.web;

import org.eclipse.jetty.http.MimeTypes;
import org.finos.waltz.common.EnumUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.model.*;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.endpoints.auth.AuthenticationUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.ResponseTransformer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.JacksonUtilities.getJsonMapper;
import static org.finos.waltz.common.ObjectUtilities.firstNotNull;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.service.user.RoleUtilities.getRequiredRoleForEntityKind;

public class WebUtilities {

    private static final Logger LOG = LoggerFactory.getLogger(WebUtilities.class);

    public static final String TYPE_JSON = "application/json";

    private static final MimeTypes mimeTypes = new MimeTypes();

    static {
        mimeTypes.addMimeMapping("ttf", "application/x-font-ttf");
    }


    public static final ResponseTransformer transformer = getJsonMapper()::writeValueAsString;


    /**
     * @see StringUtilities
     */
    public static String mkPath(String... segs) {
        return StringUtilities.mkPath(segs);
    }


    /**
     * Will render the given object to the response object, or else report failure
     * to the given logger.
     * @param res  spark response object
     * @param obj  object to render
     * @param logger   logger to use if failure report required
     */
    public static void attemptRender(Response res,
                                     Object obj,
                                     Logger logger) {
        checkNotNull(res, "res must not be null");
        checkNotNull(obj, "obj must not be null");
        checkNotNull(logger, "logger must not be null");

        try {
            String msg = transformer.render(obj);
            res.body(msg);
        } catch (Exception e) {
            logger.warn("Failed to render {} because: {}", obj, e.getMessage());
        }
    }


    public static long getLong(Request request,
                               String paramName) {
        checkNotNull(request, "request must not be null");
        checkNotNull(paramName, "paramName must not be null");

        return Long.parseLong(request.params(paramName));
    }


    public static long getId(Request request) {
        return getLong(request, "id");
    }


    public static void requireEditRoleForEntity(UserRoleService userRoleService,
                                                Request req,
                                                EntityKind kind,
                                                Operation op,
                                                EntityKind additionalKind) {
        requireRole(
                userRoleService,
                req,
                getRequiredRoleForEntityKind(kind, op, additionalKind));
    }


    public static void requireRole(UserRoleService userRoleService,
                                   Request request,
                                   SystemRole... requiredRoles) {
        requireRole(userRoleService, request, SetUtilities.map(asSet(requiredRoles), Enum::name));
    }


    public static void requireRole(UserRoleService userRoleService,
                                   Request request,
                                   String... requiredRoles) {
        requireRole(userRoleService, request, asSet(requiredRoles));
    }

    public static void requireRole(UserRoleService userRoleService,
                                   Request request,
                                   Set<String> requiredRoles) {
        String user = getUsername(request);
        if (StringUtilities.isEmpty(user)) {
            LOG.warn("Required role check failed as no user, roles needed: " + requiredRoles);
            throw new IllegalArgumentException("Not logged in");
        }
        if (! userRoleService.hasRole(user, requiredRoles)) {
            LOG.warn("Required role check failed as user: " + user + ", did not have required roles: " + requiredRoles);
            throw new NotAuthorizedException();
        }
    }


    public static void requireAnyRole(UserRoleService userRoleService,
                                      Request request,
                                      SystemRole... requiredRoles) {
        String user = getUsername(request);
        if (StringUtilities.isEmpty(user)) {
            LOG.warn("Required role check failed as no user, roles needed: " + Arrays.toString(requiredRoles));
            throw new IllegalArgumentException("Not logged in");
        }
        if (!userRoleService.hasAnyRole(user, requiredRoles)) {
            LOG.warn("Required role check failed as user: " + user + ", did not have any of required roles: " + Arrays.toString(requiredRoles));
            throw new NotAuthorizedException();
        }
    }


    public static String getUsername(Request request) {
        return AuthenticationUtilities.getUsername(request);
    }


    /**
     * Expect parameter to be called: <code>kind</code>
     * @param request Http request
     * @return Entity Kind parsed from the param named 'kind'
     */
    public static EntityKind getKind(Request request) {
       return getKind(request, "kind");
    }


    /**
     * Expect parameter to be called: <code>kind</code>
     * @param request Http request
     * @param paramName Name of the paramter which contains the entity kind value
     * @return The parsed entity kind
     */
    public static EntityKind getKind(Request request, String paramName) {
        checkNotNull(request, "request must not be null");
        return EntityKind.valueOf(request.params(paramName));
    }


    /**
     * Expects parameters :kind and :id
     * @param request Http request
     * @return EntityReferenced parsed from parameters 'kind' and 'id'
     */
    public static EntityReference getEntityReference(Request request) {
        return getEntityReference(request, "kind", "id");
    }


    /**
     * Expects parameters :kind and :id
     * @param request Http request
     * @param kindParamName Name of the parameter containing the entity kind
     * @param idParamName Name of the parameter containing the (Long) id
     * @return Entity Reference parsed from the kind and id params
     */
    public static EntityReference getEntityReference(Request request, String kindParamName, String idParamName) {
        checkNotNull(request, "request must not be null");
        return mkRef(
                getKind(request, kindParamName),
                getLong(request, idParamName));
    }


    /**
     * Reads the body of the request and attempts to convert it into an instance of
     * the given class.
     *
     * @param request Http request
     * @param objClass Class of the object we are parsing
     * @param <T> Object type
     * @return Instance of the object parsed from the request body
     * @throws IOException If the object representation could not be parsed
     */
    public static <T> T readBody(Request request,
                                 Class<T> objClass) throws IOException {
        return getJsonMapper().readValue(
                request.bodyAsBytes(),
                objClass);
    }


    public static <T> List<T> readList(Request request, Class<T> itemClass) throws IOException {
        return (List<T>) readBody(request, List.class);
    }



    public static List<Long> readIdsFromBody(Request req) throws IOException {
        List list = readBody(req, List.class);

        return (List<Long>) list
                .stream()
                .map(l -> Long.parseLong(l.toString()))
                .collect(Collectors.toList());
    }


    public static List<String> readStringsFromBody(Request req) throws IOException {
        return readBody(req, List.class);
    }


    public static IdSelectionOptions readIdSelectionOptionsFromBody(Request request) throws java.io.IOException {
        return readBody(request, IdSelectionOptions.class);
    }




    /**
     * Reads the body of the request and attempts to convert it into an instance of
     * the given class. If the attempt fails then return the given default object
     * instead of throwing an exception.
     *
     * @param request http request
     * @param objClass Class of the object we are parsing
     * @param <T> Object type
     * @param dflt An instance of T to return if parsing fails
     * @return Instance of the object parsed from the request body
     */
    public static <T> T readBody(Request request,
                                 Class<T> objClass,
                                 T dflt) {
        try {
            return readBody(request, objClass);
        } catch (IOException ioe) {
            String msg = "Failed to ready body of request into an object of type: " + objClass.getName() + ", returning default object";
            LOG.warn(msg, ioe);
            return dflt;
        }
    }


    public static <T extends Enum<T>> T readEnum(Request request,
                                                 String paramName,
                                                 Class<T> enumClass,
                                                 Function<String, T> failedParseSupplier) {
        String paramValue = request.params(paramName);
        return EnumUtilities.readEnum(paramValue, enumClass, failedParseSupplier);
    }


    public static void reportException(int statusCode,
                                       Optional<String> maybeErrorCode,
                                       String message,
                                       Response res,
                                       Logger log) {
        res.status(statusCode);
        ImmutableWebError webError = ImmutableWebError.builder()
                .message(message)
                .id(maybeErrorCode)
                .build();
        attemptRender(res, webError, log);

    }


    public static void reportException(int statusCode,
                                       String errorCode,
                                       String message,
                                       Response res,
                                       Logger log) {
        reportException(statusCode, Optional.ofNullable(errorCode), message, res, log);
    }


    /**
     * Given a path will return the mime type to go with it.
     * Does this by looking at the extension of the final (non-anchor)
     * segment of the given path.  <br>
     *
     * To add new mimetypes modify the static initialiser of this class.
     *
     * @param path
     * @return
     */
    public static String getMimeType(String path) {
        return firstNotNull(
                mimeTypes.getMimeByExtension(path),
                "application/octet-stream");
    }


    public static Optional<Integer> getLimit(Request request) {
        String limitVal = request.queryParams("limit");
        return Optional
                .ofNullable(limitVal)
                .map(Integer::valueOf);
    }


    public static Optional<java.util.Date> getDateParam(Request request) {
        String dateVal = request.queryParams("date");
        return Optional
                .ofNullable(dateVal)
                .map(s -> {
                    try {
                        return new SimpleDateFormat("yyyy-MM-dd").parse(s);
                    } catch (ParseException pe) {
                        LOG.warn("Could not parse date: " + s);
                        return null;
                    }
                });

    }

    public static Optional<LocalDate> getLocalDateParam(Request request, String paramName) {
        String dateVal = request.params(paramName);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return Optional
                .ofNullable(dateVal)
                .map(s -> {
                    try {
                        return LocalDate.parse(s, formatter);
                    } catch (DateTimeParseException pe) {
                        LOG.warn("Could not parse date: " + s);
                        return null;
                    }
                });
    }

    
    /**
     * Helper method to flatten a map (m) into a list of Entry's.
     * Typically used when the key (K) is a complex type
     * as json object may only have simple keys.
     * @param m
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> List<Entry<K, V>> simplifyMapToList(Map<K, V> m) {
        checkNotNull(m, "Map m cannot be null");

        return m.entrySet()
                .stream()
                .map(entry -> (Entry<K,V>) ImmutableEntry.builder()
                        .key(entry.getKey())
                        .value(entry.getValue())
                        .build())
                .collect(toList());
    }

}
