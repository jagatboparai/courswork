package com.mlops.exception;

import com.mlops.model.ApiError;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

// =============================================================================
// 5.1 WorkspaceNotEmptyException → 409 Conflict
// =============================================================================

@Provider
class WorkspaceNotEmptyExceptionMapper implements ExceptionMapper<WorkspaceNotEmptyException> {

    private static final Logger LOGGER = Logger.getLogger(WorkspaceNotEmptyExceptionMapper.class.getName());

    @Override
    public Response toResponse(WorkspaceNotEmptyException ex) {
        LOGGER.warning("409 Conflict — " + ex.getMessage());
        ApiError error = new ApiError(
                409,
                "Conflict",
                ex.getMessage() + " Remove or reassign all models before deleting the workspace."
        );
        return Response.status(Response.Status.CONFLICT)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}

// =============================================================================
// 5.2 LinkedWorkspaceNotFoundException → 422 Unprocessable Entity
// =============================================================================

@Provider
class LinkedWorkspaceNotFoundExceptionMapper implements ExceptionMapper<LinkedWorkspaceNotFoundException> {

    private static final Logger LOGGER = Logger.getLogger(LinkedWorkspaceNotFoundExceptionMapper.class.getName());

    @Override
    public Response toResponse(LinkedWorkspaceNotFoundException ex) {
        LOGGER.warning("422 Unprocessable Entity — " + ex.getMessage());
        ApiError error = new ApiError(422, "Unprocessable Entity", ex.getMessage());
        return Response.status(422)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}

// =============================================================================
// 5.3 ModelDeprecatedException → 403 Forbidden
// =============================================================================

@Provider
class ModelDeprecatedExceptionMapper implements ExceptionMapper<ModelDeprecatedException> {

    private static final Logger LOGGER = Logger.getLogger(ModelDeprecatedExceptionMapper.class.getName());

    @Override
    public Response toResponse(ModelDeprecatedException ex) {
        LOGGER.warning("403 Forbidden — " + ex.getMessage());
        ApiError error = new ApiError(403, "Forbidden", ex.getMessage());
        return Response.status(Response.Status.FORBIDDEN)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}

// =============================================================================
// ResourceNotFoundException → 404 Not Found
// =============================================================================

@Provider
class ResourceNotFoundExceptionMapper implements ExceptionMapper<ResourceNotFoundException> {

    private static final Logger LOGGER = Logger.getLogger(ResourceNotFoundExceptionMapper.class.getName());

    @Override
    public Response toResponse(ResourceNotFoundException ex) {
        LOGGER.warning("404 Not Found — " + ex.getMessage());
        ApiError error = new ApiError(404, "Not Found", ex.getMessage());
        return Response.status(Response.Status.NOT_FOUND)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}

// =============================================================================
// 5.4 Global Safety Net → 500 Internal Server Error
// Catches any unexpected Throwable that is not already handled by a more
// specific mapper. JAX-RS selects the most specific mapper first, so this
// only fires when no other mapper matches.
// =============================================================================

@Provider
class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {
        LOGGER.log(Level.SEVERE, "Unhandled exception intercepted by global safety net", ex);
        ApiError error = new ApiError(
                500,
                "Internal Server Error",
                "An unexpected error occurred. Please contact the API administrator."
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
