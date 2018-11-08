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

package com.khartec.waltz.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.khartec.waltz.common.EnumUtilities;
import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.endpoints.auth.AuthenticationUtilities;
import org.eclipse.jetty.http.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.ResponseTransformer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkAll;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ObjectUtilities.firstNotNull;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.service.user.RoleUtilities.getRequiredRoleForEntityKind;
import static java.util.stream.Collectors.toList;

public class WebUtilities {


    private static final Logger LOG = LoggerFactory.getLogger(WebUtilities.class);

    public static final String TYPE_JSON = "application/json";

    private static final MimeTypes mimeTypes = new MimeTypes();
    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.registerModule(new JSR310Module()); // DateTime etc
        mapper.registerModule(new Jdk8Module()); // Optional etc

        // Force timestamps to be sent as ISO-8601 formatted strings
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        mimeTypes.addMimeMapping("ttf", "application/x-font-ttf");
    }


    public static final ResponseTransformer transformer = mapper::writeValueAsString;


    /**
     * Given a vararg/array of path segments will join them
     * to make a string representing the path.  No starting or trailing
     * slashes are added to the resultant path string.
     *
     * @param segs Segments to join
     * @return String representing the path produced by joining the segments
     * @throws IllegalArgumentException If any of the segments are null
     */
    public static String mkPath(String... segs) {
        checkAll(
                segs,
                x -> StringUtilities.notEmpty(x),
                "Cannot convert empty segments to path");

        return String.join("/", segs);
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
                                   Role... requiredRoles) {
        String user = getUsername(request);
        if (StringUtilities.isEmpty(user)) {
            LOG.warn("Required role check failed as no user, roles needed: " + Arrays.toString(requiredRoles));
            throw new IllegalArgumentException("Not logged in");
        }
        if (! userRoleService.hasRole(user, requiredRoles)) {
            LOG.warn("Required role check failed as user: " + user + ", did not have required roles: " + Arrays.toString(requiredRoles));
            throw new NotAuthorizedException();
        }
    }


    public static void requireAnyRole(UserRoleService userRoleService,
                                      Request request,
                                      Role... requiredRoles) {
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
        return mapper.readValue(
                request.bodyAsBytes(),
                objClass);
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


    public static EntityIdSelectionOptions readEntityIdOptionsFromBody(Request request) throws java.io.IOException {
        return readBody(request, EntityIdSelectionOptions.class);
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
                .map(s -> Integer.valueOf(s));
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
