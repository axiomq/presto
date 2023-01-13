/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.features.http;

import com.facebook.presto.features.binder.PrestoFeatureToggle;
import com.google.inject.Inject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Path("/v1/features")
public class FeatureToggleInfo
{
    private final PrestoFeatureToggle featureToggle;

    @Inject
    public FeatureToggleInfo(PrestoFeatureToggle featureToggle)
    {
        this.featureToggle = featureToggle;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response info(@QueryParam("enabled") Boolean enabled, @QueryParam("id") String id)
    {
        List<FeatureInfo> infoList = FeatureInfo.featureToggle(featureToggle);
        if (enabled != null) {
            infoList = infoList.stream()
                    .filter(FeatureInfo::isEnabled)
                    .collect(Collectors.toList());
        }
        if (id != null) {
            infoList = infoList.stream()
                    .filter(f -> f.getFeatureId().toLowerCase(Locale.US).contains(id.toLowerCase(Locale.US)))
                    .collect(Collectors.toList());
        }
        return Response.ok().entity(infoList).build();
    }

    @GET
    @Path("/{featureId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response details(@PathParam("featureId") String featureId)
    {
        if (featureToggle.getFeatureMap().get(featureId) != null) {
            return Response.ok().entity(FeatureDetails.details(featureToggle, featureId, featureToggle.getFeatureToggleConfiguration())).build();
        }
        return Response.noContent().build();
    }
}
