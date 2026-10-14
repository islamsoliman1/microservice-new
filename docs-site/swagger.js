"use strict";

window.onload = function () {
  SwaggerUIBundle({
    url: "./openapi.json",
    dom_id: "#swagger-ui",
    presets: [SwaggerUIBundle.presets.apis],
    plugins: [() => ({
      components: {
        authorizeBtn: () => null,
        authorizationPopup: () => null
      }
    })],
    layout: "BaseLayout",
    deepLinking: true,
    filter: true,
    operationsSorter: "method",
    supportedSubmitMethods: [],
    tryItOutEnabled: false,
    persistAuthorization: false,
    validatorUrl: null,
    requestInterceptor: (request) => {
      const target = new URL(request.url, window.location.href);
      if (target.href !== new URL("./openapi.json", window.location.href).href) {
        throw new Error("This hosted API reference does not execute backend requests.");
      }
      return request;
    }
  });
};
