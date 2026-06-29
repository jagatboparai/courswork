package com.mlops.application;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS Application bootstrap class.
 *
 * @ApplicationPath("/api/v1") establishes the versioned entry point for all resources.
 * Jersey auto-discovers all @Provider and @Path annotated classes on the classpath,
 * so no explicit class registration is required.
 */
@ApplicationPath("/api/v1")
public class MLOpsApplication extends Application {
    // Jersey scans the classpath automatically — no getClasses() override needed.
}
