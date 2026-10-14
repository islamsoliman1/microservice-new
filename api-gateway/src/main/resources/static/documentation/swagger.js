"use strict";

function InternalReferencePlugin() {
  return {
    wrapComponents: {
      operation: (Original, system) => (props) => {
        const path = props.operation.get("path");
        const method = props.operation.get("method");
        const internal = system.specSelectors.specJson().getIn(["paths", path, method, "x-internal"]);
        const operation = internal
          ? props.operation.set("allowTryItOut", false).set("tryItOutEnabled", false)
          : props.operation;
        return system.React.createElement(Original, { ...props, operation });
      }
    }
  };
}

window.onload = function () {
  window.ui = SwaggerUIBundle({
    url: "/openapi/all",
    dom_id: "#swagger-ui",
    presets: [SwaggerUIBundle.presets.apis],
    plugins: [InternalReferencePlugin],
    layout: "BaseLayout",
    deepLinking: true,
    filter: true,
    operationsSorter: "method",
    displayRequestDuration: true,
    persistAuthorization: false,
    validatorUrl: null,
    requestInterceptor: (request) => {
      const path = new URL(request.url, window.location.origin).pathname;
      if (path === "/api/inventory" || path.startsWith("/api/inventory/")) {
        throw new Error("Inventory is an internal reference and cannot be executed from this page.");
      }
      return request;
    }
  });
};
