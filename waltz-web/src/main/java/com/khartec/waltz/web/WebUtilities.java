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

package com.khartec.waltz.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.khartec.waltz.common.EnumUtilities;
import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.ImmutableWebError;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.model.user.User;
import com.khartec.waltz.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.ResponseTransformer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkAll;
import static com.khartec.waltz.common.Checks.checkNotNull;

public class WebUtilities {


    private static final Logger LOG = LoggerFactory.getLogger(WebUtilities.class);

    public static final String TYPE_JSON = "application/json";

    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.registerModule(new JSR310Module()); // DateTime etc
        mapper.registerModule(new Jdk8Module()); // Optional etc

        // Force timestamps to be sent as ISO-8601 formatted strings
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
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
                "Cannot convert segs to path");

        return String.join("/", segs);
    }


    /**
     * Will render the given object to the response object, or else report failure
     * to the given logger.
     * @param res  spark response object
     * @param obj  object to render
     * @param logger   logger to use if failure report required
     */
    public static void attemptRender(Response res, Object obj, Logger logger) {
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


    public static long getLong(Request request, String paramName) {
        checkNotNull(request, "request must not be null");
        checkNotNull(paramName, "paramName must not be null");

        return Long.parseLong(request.params(paramName));
    }


    public static long getId(Request request) {
        return getLong(request, "id");
    }


    public static void requireRole(UserService userService, Request request, Role... requiredRoles) {
        User user = getUser(request);
        if (user == null) {
            LOG.warn("Required role check failed as no user, roles needed: " + requiredRoles);
            throw new IllegalArgumentException("Not logged in");
        }
        if (! userService.hasRole(user.userName(), requiredRoles)) {
            LOG.warn("Required role check failed as user: " + user + ", did not have required roles: " + Arrays.toString(requiredRoles));
            throw new NotAuthorizedException();
        }
    }


    public static User getUser(Request request) {
        return (User) request.attribute("user");
    }


    /**
     * Expect parameter to be called: <code>kind</code>
     * @param request
     * @return
     */
    public static EntityKind getKind(Request request) {
        checkNotNull(request, "request must not be null");

        return EntityKind.valueOf(request.params("kind"));
    }


    /**
     * Expects parameters :kind and :id
     * @param request
     * @return
     */
    public static EntityReference getEntityReference(Request request) {
        checkNotNull(request, "request must not be null");
        return ImmutableEntityReference.builder()
                .kind(getKind(request))
                .id(getId(request))
                .build();
    }


    public static <T> T readBody(Request request, Class<T> objClass) throws IOException {
        return mapper.readValue(
                request.bodyAsBytes(),
                objClass);
    }

    public static <T extends Enum<T>> T readEnum(Request request,
                               String paramName,
                               Class<T> enumClass,
                               T dflt) {
        return EnumUtilities.readEnum(request.params(paramName), enumClass, dflt);
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
}
