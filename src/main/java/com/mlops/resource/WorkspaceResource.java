package com.mlops.resource;

import com.mlops.exception.ResourceNotFoundException;
import com.mlops.exception.WorkspaceNotEmptyException;
import com.mlops.model.DataStore;
import com.mlops.model.MLWorkspace;

import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Part 2 — Workspace Management
 *
 * Manages the /api/v1/workspaces resource collection.
 */
@Path("/workspaces")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WorkspaceResource {

    private final DataStore store = DataStore.getInstance();

    // -------------------------------------------------------------------------
    // GET /api/v1/workspaces — list all workspaces
    // Includes Cache-Control headers to reduce redundant server processing.
    // -------------------------------------------------------------------------
    @GET
    public Response getAllWorkspaces() {
        List<MLWorkspace> list = new ArrayList<>(store.getWorkspaces().values());

        // Cache-Control: max-age=30 tells clients to cache the list for 30 seconds.
        // This avoids hammering the server for every dashboard refresh.
        CacheControl cc = new CacheControl();
        cc.setMaxAge(30);
        cc.setNoTransform(true);

        return Response.ok(list).cacheControl(cc).build();
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/workspaces — create a new workspace
    // Server generates the ID; client must supply teamName and storageQuotaGb.
    // -------------------------------------------------------------------------
    @POST
    public Response createWorkspace(MLWorkspace input) {
        if (input == null || input.getTeamName() == null || input.getTeamName().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Bad Request", "message", "'teamName' is required."))
                    .build();
        }

        // Server-generated ID for integrity (no client-supplied IDs)
        String id = "WS" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        input.setId(id);
        input.setModelIds(new ArrayList<>());

        store.putWorkspace(input);

        return Response.status(Response.Status.CREATED).entity(input).build();
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/workspaces/{workspaceId} — fetch a specific workspace
    // -------------------------------------------------------------------------
    @GET
    @Path("/{workspaceId}")
    public Response getWorkspace(@PathParam("workspaceId") String workspaceId) {
        MLWorkspace ws = store.getWorkspace(workspaceId);
        if (ws == null) {
            throw new ResourceNotFoundException("Workspace", workspaceId);
        }
        return Response.ok(ws).build();
    }

    // -------------------------------------------------------------------------
    // HEAD /api/v1/workspaces/{workspaceId}
    // Allows clients to check existence without downloading the full JSON body.
    // Returns 200 if found, 404 if not — with no response body (per HTTP spec).
    // -------------------------------------------------------------------------
    @HEAD
    @Path("/{workspaceId}")
    public Response headWorkspace(@PathParam("workspaceId") String workspaceId) {
        MLWorkspace ws = store.getWorkspace(workspaceId);
        if (ws == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok().build();
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/workspaces/{workspaceId}
    // Business Rule: Cannot delete a workspace that still has models assigned.
    // Throws WorkspaceNotEmptyException → mapped to 409 Conflict.
    // -------------------------------------------------------------------------
    @DELETE
    @Path("/{workspaceId}")
    public Response deleteWorkspace(@PathParam("workspaceId") String workspaceId) {
        MLWorkspace ws = store.getWorkspace(workspaceId);
        if (ws == null) {
            throw new ResourceNotFoundException("Workspace", workspaceId);
        }

        // Safety constraint: block deletion if models are still linked
        if (!ws.getModelIds().isEmpty()) {
            throw new WorkspaceNotEmptyException(workspaceId, ws.getModelIds().size());
        }

        store.deleteWorkspace(workspaceId);

        return Response.noContent().build(); // 204 No Content — successful deletion
    }
}
