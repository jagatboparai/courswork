## Question Answers

### Part 1.1 — Role of MessageBodyWriter / JSON Provider

When a JAX-RS resource method returns a Java object (e.g., a `MachineLearningModel` instance), the framework does not know how to convert that POJO into an HTTP response body on its own. This is where a `MessageBodyWriter<T>` comes in. It is an interface in the JAX-RS specification that declares one method, `writeTo(...)`, responsible for serialising an object of type `T` into an `OutputStream`.

Jackson is the most widely used JSON provider in Jersey. When `jersey-media-json-jackson` is registered, it contributes a `JacksonJsonProvider` which implements `MessageBodyWriter`. When the runtime needs to serialise the return value, it looks through all registered writers, asks each one whether it `isWriteable(...)` for the given class and media type (`application/json`), and picks the first match. Jackson's implementation uses `ObjectMapper` to introspect the POJO's fields and getters, then writes their values as a JSON object to the response output stream. The `@Produces(MediaType.APPLICATION_JSON)` annotation on the resource method signals to the runtime which `MessageBodyWriter` to select.


### Part 1.2 — REST Statelessness and Horizontal Scaling

Statelessness in REST means that every HTTP request from a client must contain all information necessary to understand and process that request. The server retains no session state between requests — it does not remember previous interactions.

This makes horizontal scaling straightforward because any server instance can handle any request; there is no session affinity or "sticky session" requirement. When load increases, new server instances can be added to the pool, and a load balancer can freely route requests across all of them. If one instance crashes, no session data is lost because none was stored there. Contrast this with stateful designs where a user's session is tied to a specific server — adding more servers requires complex session replication or a shared session store. Statelessness eliminates this coupling entirely, making cloud auto-scaling and server restarts transparent to the client.


### Part 2.1 — HTTP Cache-Control Headers

The `GET /api/v1/workspaces` endpoint returns a list that does not change on every millisecond. Without caching, every client polling a dashboard triggers a full server-side computation and response serialisation. By adding `Cache-Control: max-age=30, no-transform` headers:

- **Client-side benefit**: The client (or an intermediate CDN/proxy) can serve the cached response for up to 30 seconds without issuing a new request, reducing latency and bandwidth.
- **Server-side benefit**: The server receives far fewer requests during the cache window, reducing CPU usage for serialisation and reducing thread pool pressure.

For resources with higher update frequency, `Cache-Control: no-cache` with an `ETag` or `Last-Modified` header enables conditional requests — the client revalidates cheaply using `If-None-Match`, and the server returns `304 Not Modified` (no body) if unchanged.


### Part 2.2 — HEAD vs GET for Existence Checks

A client should use `HEAD` instead of `GET`. The `HEAD` method is semantically identical to `GET` — it follows the same path, triggers the same server logic, and returns the same status code and headers — but the HTTP specification mandates that the server must **not** include a message body in a `HEAD` response.

This is valuable when the client only needs to determine whether a resource exists (checking the status code: `200 OK` vs `404 Not Found`) without the overhead of downloading, parsing, and discarding the JSON body. For large workspace objects with many model IDs, this can represent significant bandwidth saving, especially in automated health-check loops or polling scenarios.


### Part 3.1 — Server-Generated IDs: Security and Data Integrity

Allowing clients to supply their own `id` values introduces several risks:

1. **ID collision / overwrite attacks**: A malicious or buggy client could supply the ID of an existing model, causing an implicit overwrite of another team's resource without any `PUT` semantics or conflict detection.
2. **Predictable enumeration**: Client-chosen sequential IDs (e.g., `MOD-1`, `MOD-2`) are trivially guessable, enabling unauthorised enumeration of all resources.
3. **Data integrity**: The server is the authoritative source of truth for resource identity. Delegating ID generation to the client breaks this invariant and can lead to inconsistent foreign-key relationships (e.g., a `workspaceId` referencing a workspace that was never created).

By generating IDs server-side with `UUID.randomUUID()`, the server guarantees global uniqueness, prevents collisions, and ensures that only resources the server deliberately created are addressable.


### Part 3.2 — URL Encoding for Special Characters

If a user queries `?framework=Scikit Learn & Tools`, the space and `&` character break the URL structure. HTTP query strings use `&` as a delimiter between parameters, so the string above would be parsed as two separate parameters: `framework=Scikit Learn ` and `Tools` (with no value). The space is also illegal in a URI.

The client must **percent-encode** (URL-encode) the value: `?framework=Scikit%20Learn%20%26%20Tools`. The space becomes `%20` and `&` becomes `%26`. This encoding is defined in RFC 3986. It ensures the entire value is treated as a single opaque string by every component in the chain — the browser, any proxies, and the server. JAX-RS's `@QueryParam` handles decoding automatically, so the resource method receives the original string.


### Part 4.1 — Class-Level vs Method-Level `@Produces`

Placing `@Produces(MediaType.APPLICATION_JSON)` at the **class level** acts as a default for all methods in that class. This eliminates repetition — every method implicitly produces JSON without needing its own annotation. It clearly communicates the resource's primary contract.

A **method-level** `@Produces` annotation **overrides** the class-level one for that specific method only. For example, if the class declares `@Produces(APPLICATION_JSON)` but one method needs to return a CSV export, that method can declare `@Produces("text/csv")` and the JAX-RS runtime will use `text/csv` for content negotiation on that endpoint alone, while all other methods continue to use the class-level default.


### Part 5.2 — 4xx vs 5xx for Validation Failures

HTTP status codes are divided into classes by their first digit:
- **4xx (Client Error)**: The request was understood by the server, but the **client** provided invalid or unacceptable data. The fault lies with the request.
- **5xx (Server Error)**: The server **itself** failed to fulfil a valid request due to an unexpected condition on the server side.

When a client submits a `workspaceId` that does not exist, the server correctly processed the request — it checked the store and determined the referenced workspace is absent. This is a **client error**: the client provided a value that violates a business constraint. Returning `5xx` would be misleading, implying the server is broken or experiencing an internal fault. A `422 Unprocessable Entity` tells the client precisely: "your request was syntactically valid, but semantically incorrect — fix your data and retry."


### Part 5.4 — Exception Mapper Resolution Priority

The JAX-RS runtime uses a **most-specific-type-wins** strategy when selecting an `ExceptionMapper`. When an exception is thrown, it walks the exception's class hierarchy from the most specific type upward towards `Throwable`. It selects the first registered mapper whose generic type parameter is assignable from the thrown exception type.

For example, if `LinkedWorkspaceNotFoundException` is thrown and both `ExceptionMapper<LinkedWorkspaceNotFoundException>` and `ExceptionMapper<Throwable>` are registered, the runtime picks `ExceptionMapper<LinkedWorkspaceNotFoundException>` because it is a more specific match. The global `ExceptionMapper<Throwable>` only fires when no more specific mapper is found — for example, a `NullPointerException` or `ArrayIndexOutOfBoundsException` that has no dedicated mapper.


### Part 5.5 — HTTP Metadata from Filter Contexts

Two highly valuable pieces of metadata extractable from the filter contexts:

1. **Request URI + Query String** (`ContainerRequestContext.getUriInfo().getRequestUri()`): The full URI including path and query parameters is the most critical piece of debugging information. It tells you exactly what resource was accessed and with what filter arguments, enabling you to reproduce the failing request precisely.

2. **Response Status Code** (`ContainerResponseContext.getStatus()`): Knowing the final HTTP status code in the response filter allows you to immediately flag any `4xx` or `5xx` responses in logs without needing to inspect the body. This is the foundation of automated alerting — log aggregation tools like Datadog or Splunk can create dashboards and alerts based purely on status code frequency from structured log lines.
