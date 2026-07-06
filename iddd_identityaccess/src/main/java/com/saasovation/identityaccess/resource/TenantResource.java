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
import jakarta.ws.rs.core.Response;

import com.saasovation.common.media.OvationsMediaType;
import com.saasovation.common.serializer.ObjectSerializer;
import com.saasovation.identityaccess.domain.model.identity.Tenant;

@Path("/tenants")
public class TenantResource extends AbstractResource {

    public TenantResource() {
        super();
    }

    @GET
    @Path("{tenantId}")
    @Produces({ OvationsMediaType.ID_OVATION_TYPE })
    public Response getTenant(
            @PathParam("tenantId") String aTenantId) {

        Tenant tenant = this.identityApplicationService().tenant(aTenantId);

        if (tenant == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        String tenantRepresentation = ObjectSerializer.instance().serialize(tenant);

        Response response =
                Response
                    .ok(tenantRepresentation)
                    .cacheControl(this.cacheControlFor(3600))
                    .build();

        return response;
    }
}
