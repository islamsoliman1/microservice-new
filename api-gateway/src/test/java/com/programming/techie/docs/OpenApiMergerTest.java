package com.programming.techie.docs;

import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import static org.junit.jupiter.api.Assertions.*;

class OpenApiMergerTest {
    private final JsonMapper mapper = new JsonMapper();
    private final OpenApiMerger merger = new OpenApiMerger(mapper);

    private ObjectNode definition(String path, String type) {
        return (ObjectNode) mapper.readTree("""
                {
                  "openapi": "3.1.0",
                  "info": {"description": "Example"},
                  "security": [{"bearerAuth": []}],
                  "paths": {
                    "%s": {"get": {
                      "operationId": "lookup",
                      "responses": {"200": {"description": "Result", "content": {
                        "application/json": {"schema": {"$ref": "#/components/schemas/Result"}}
                      }}}
                    }}
                  },
                  "components": {
                    "schemas": {"Result": {"type": "%s"}},
                    "securitySchemes": {"bearerAuth": {"type": "http", "scheme": "bearer"}}
                  }
                }
                """.formatted(path, type));
    }

    @Test
    void preservesCollidingSchemasAndTheirReferencesWithoutMutatingSources() {
        ObjectNode product = definition("/api/product", "string");
        ObjectNode inventory = definition("/api/inventory", "boolean");
        ObjectNode combined = merger.merge(List.of(
                new OpenApiMerger.Source("product", "Products", false, product),
                new OpenApiMerger.Source("inventory", "Inventory (Internal)", true, inventory)), "Overview");

        assertEquals("string", combined.at("/components/schemas/product_Result/type").asString());
        assertEquals("boolean", combined.at("/components/schemas/inventory_Result/type").asString());
        assertEquals("#/components/schemas/product_Result",
                combined.at("/paths/~1api~1product/get/responses/200/content/application~1json/schema/$ref").asString());
        assertEquals("#/components/schemas/inventory_Result",
                combined.at("/paths/~1api~1inventory/get/responses/200/content/application~1json/schema/$ref").asString());
        assertEquals(1, combined.at("/components/securitySchemes").size());
        assertEquals("product_lookup", combined.at("/paths/~1api~1product/get/operationId").asString());
        assertEquals("inventory_lookup", combined.at("/paths/~1api~1inventory/get/operationId").asString());
        assertTrue(combined.at("/paths/~1api~1inventory/get/x-internal").asBoolean());
        assertTrue(combined.at("/paths/~1api~1inventory/get/security").isEmpty());
        assertTrue(combined.at("/paths/~1api~1product/get/security/0").has("bearerAuth"));
        assertTrue(product.at("/components/schemas").has("Result"));
        assertEquals("lookup", product.at("/paths/~1api~1product/get/operationId").asString());
    }

    @Test
    void rewritesNestedReferencesDiscriminatorMappingsAndEscapedComponentNames() {
        ObjectNode source = definition("/api/product", "object");
        ObjectNode schema = (ObjectNode) source.at("/components/schemas/Result");
        source.at("/components/schemas").asObject().putObject("Shared/Result").put("type", "string");
        schema.putObject("properties").putObject("value").put("$ref", "#/components/schemas/Shared~1Result");
        schema.putObject("discriminator").put("propertyName", "type")
                .putObject("mapping").put("shared", "#/components/schemas/Shared~1Result");
        ObjectNode combined = merger.merge(
                List.of(new OpenApiMerger.Source("product", "Products", false, source)), "Overview");
        assertEquals("#/components/schemas/product_Shared~1Result",
                combined.at("/components/schemas/product_Result/properties/value/$ref").asString());
        assertEquals("#/components/schemas/product_Shared~1Result",
                combined.at("/components/schemas/product_Result/discriminator/mapping/shared").asString());
    }

    @Test
    void rejectsDuplicatePathsAndUnresolvedReferences() {
        ObjectNode source = definition("/api/product", "string");
        assertThrows(IllegalArgumentException.class, () -> merger.merge(List.of(
                new OpenApiMerger.Source("a", "A", false, source),
                new OpenApiMerger.Source("b", "B", false, source)), "Overview"));
        source.at("/components/schemas/Result").asObject().put("$ref", "#/components/schemas/Missing");
        assertThrows(IllegalArgumentException.class, () -> merger.merge(
                List.of(new OpenApiMerger.Source("product", "Products", false, source)), "Overview"));
    }

    @Test
    void rejectsIncompatibleVersionsAndConflictingSecurityDefinitions() {
        ObjectNode product = definition("/api/product", "string");
        ObjectNode order = definition("/api/order", "string");
        order.put("openapi", "3.0.1");
        assertThrows(IllegalArgumentException.class, () -> merger.merge(List.of(
                new OpenApiMerger.Source("product", "Products", false, product),
                new OpenApiMerger.Source("order", "Orders", false, order)), "Overview"));
        order.put("openapi", "3.1.0");
        order.at("/components/securitySchemes/bearerAuth").asObject().put("scheme", "basic");
        assertThrows(IllegalArgumentException.class, () -> merger.merge(List.of(
                new OpenApiMerger.Source("product", "Products", false, product),
                new OpenApiMerger.Source("order", "Orders", false, order)), "Overview"));
    }
}
