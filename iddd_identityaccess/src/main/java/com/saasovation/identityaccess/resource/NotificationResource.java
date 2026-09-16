//   Copyright 2012,2013 Vaughn Vernon
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package com.saasovation.identityaccess.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import com.saasovation.common.media.Link;
import com.saasovation.common.media.OvationsMediaType;
import com.saasovation.common.notification.NotificationLog;
import com.saasovation.common.serializer.ObjectSerializer;
import com.saasovation.identityaccess.application.representation.NotificationLogRepresentation;

@Path("/notifications")
public class NotificationResource extends AbstractResource {

    public NotificationResource() {
        super();
    }

    @GET
    @Produces({ OvationsMediaType.ID_OVATION_TYPE })
    public Response getCurrentNotificationLog(
            @Context UriInfo aUriInfo) {

        NotificationLog currentNotificationLog =
            this.notificationApplicationService()
                .currentNotificationLog();

        if (currentNotificationLog == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        Response response =
            this.currentNotificationLogResponse(
                    currentNotificationLog,
                    aUriInfo);

        return response;
    }

    @GET
    @Path("{notificationId}")
    @Produces({ OvationsMediaType.ID_OVATION_TYPE })
    public Response getNotificationLog(
            @PathParam("notificationId") String aNotificationId,
            @Context UriInfo aUriInfo) {

        NotificationLog notificationLog =
            this.notificationApplicationService()
                .notificationLog(aNotificationId);

        if (notificationLog == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        Response response =
            this.notificationLogResponse(
                    notificationLog,
                    aUriInfo);

        return response;
    }

    private Response currentNotificationLogResponse(
            NotificationLog aCurrentNotificationLog,
            UriInfo aUriInfo) {

        NotificationLogRepresentation log =
            new NotificationLogRepresentation(aCurrentNotificationLog);

        log.setLinkSelf(
                this.selfLink(aCurrentNotificationLog, aUriInfo));

        log.setLinkPrevious(
            this.previousLink(aCurrentNotificationLog, aUriInfo));

        String serializedLog = ObjectSerializer.instance().serialize(log);

        Response response =
            Response
                .ok(serializedLog)
                .cacheControl(this.cacheControlFor(60))
                .build();

        return response;
    }

    private Response notificationLogResponse(
            NotificationLog aNotificationLog,
            UriInfo aUriInfo) {

        NotificationLogRepresentation log =
            new NotificationLogRepresentation(aNotificationLog);

        log.setLinkSelf(this.selfLink(aNotificationLog, aUriInfo));

        log.setLinkNext(this.nextLink(aNotificationLog, aUriInfo));

        log.setLinkPrevious(this.previousLink(aNotificationLog, aUriInfo));

        String serializedLog = ObjectSerializer.instance().serialize(log);

        Response response =
            Response
                .ok(serializedLog)
                .cacheControl(this.cacheControlFor(3600))
                .build();

        return response;
    }

    private Link linkFor(
            String aRelationship,
            String anId,
            UriInfo aUriInfo) {

        Link link = null;

        if (anId != null) {

            UriBuilder builder = aUriInfo.getBaseUriBuilder();

            String linkUrl =
                builder
                    .path("notifications")
                    .path(anId)
                    .build()
                    .toString();

            link = new Link(
                    linkUrl,
                    aRelationship,
                    null,
                    OvationsMediaType.ID_OVATION_TYPE);
        }

        return link;
    }

    private Link nextLink(
            NotificationLog aNotificationLog,
            UriInfo aUriInfo) {
        return
            this.linkFor(
                    "next",
                    aNotificationLog.nextNotificationLogId(),
                    aUriInfo);
    }

    private Link previousLink(
            NotificationLog aNotificationLog,
            UriInfo aUriInfo) {

        return
            this.linkFor(
                    "previous",
                    aNotificationLog.previousNotificationLogId(),
                    aUriInfo);
    }

    private Link selfLink(
            NotificationLog aNotificationLog,
            UriInfo aUriInfo) {
        return
            this.linkFor(
                    "self",
                    aNotificationLog.notificationLogId(),
                    aUriInfo);
    }
}
