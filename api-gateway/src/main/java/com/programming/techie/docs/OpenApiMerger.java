package com.programming.techie.docs;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

@Component
public class OpenApiMerger {
    private static final Set<String> METHODS = Set.of(
            "get", "post", "put", "patch", "delete", "head", "options", "trace");
    private final JsonMapper mapper;

    public OpenApiMerger(JsonMapper mapper) {
        this.mapper = mapper;
    }

    public record Source(String prefix, String tag, boolean internal, JsonNode document) {}

    public ObjectNode merge(List<Source> sources, String overview) {
        ObjectNode result = mapper.createObjectNode();
        String version = sources.getFirst().document().required("openapi").asString();
        result.put("openapi", version);
        result.putObject("info").put("title", "Microservices E-Commerce API")
                .put("version", "1.0.0").put("description", overview);
        result.putArray("servers").addObject().put("url", "/").put("description", "API Gateway");
        ObjectNode paths = result.putObject("paths");
        ObjectNode components = result.putObject("components");

        for (Source source : sources) {
            JsonNode original = source.document();
            if (!version.equals(original.required("openapi").asString())
                    || !original.path("paths").isObject()) {
                throw new IllegalArgumentException("Incompatible OpenAPI document: " + source.prefix());
            }
            ObjectNode document = (ObjectNode) original.deepCopy();
            Map<String, String> references = new LinkedHashMap<>();
            document.path("components").forEachEntry((kind, entries) -> {
                if (!kind.equals("securitySchemes")) {
                    entries.forEachEntry((name, value) -> references.put(
                            "#/components/" + kind + "/" + pointer(name),
                            "#/components/" + kind + "/" + pointer(source.prefix() + "_" + name)));
                }
            });
            Map<String, String> operationIds = new LinkedHashMap<>();
            document.path("paths").forEachEntry((path, item) ->
                    item.forEachEntry((method, operation) -> {
                        if (METHODS.contains(method) && operation.has("operationId")) {
                            String id = operation.get("operationId").asString();
                            operationIds.put(id, source.prefix() + "_" + id);
                        }
                    }));
            rewrite(document, references, operationIds);

            result.withArray("tags").addObject().put("name", source.tag())
                    .put("description", original.path("info").path("description").asString(""));
            document.path("components").forEachEntry((kind, entries) -> {
                ObjectNode target = components.withObject("/" + kind);
                entries.forEachEntry((name, value) -> {
                    String key = kind.equals("securitySchemes") ? name : source.prefix() + "_" + name;
                    if (target.has(key) && !target.get(key).equals(value)) {
                        throw new IllegalArgumentException("Conflicting component: " + key);
                    }
                    target.set(key, value);
                });
            });
            document.required("paths").forEachEntry((path, item) -> {
                if (paths.has(path)) {
                    throw new IllegalArgumentException("Conflicting API path: " + path);
                }
                ObjectNode pathItem = (ObjectNode) item;
                pathItem.remove("servers");
                pathItem.forEachEntry((method, value) -> {
                    if (METHODS.contains(method)) {
                        ObjectNode operation = (ObjectNode) value;
                        operation.putArray("tags").add(source.tag());
                        operation.remove("servers");
                        if (!operation.has("security") && document.has("security")) {
                            operation.set("security", document.get("security").deepCopy());
                        }
                        if (source.internal()) {
                            operation.put("x-internal", true);
                            operation.putArray("security");
                            operation.put("description", "Internal service reference. This endpoint is not "
                                    + "routed by the gateway; interactive execution is disabled here.\n\n"
                                    + operation.path("description").asString(""));
                        }
                    }
                });
                paths.set(path, pathItem);
            });
        }
        validateReferences(result, result);
        return result;
    }

    private void rewrite(JsonNode node, Map<String, String> references, Map<String, String> operationIds) {
        if (node instanceof ObjectNode object) {
            if (object.path("$ref").isString()) {
                object.put("$ref", renamedReference(object.get("$ref").asString(), references));
            }
            if (object.path("operationId").isString()) {
                String id = object.get("operationId").asString();
                object.put("operationId", operationIds.getOrDefault(id, id));
            }
            // Discriminator mappings contain references without a $ref key.
            JsonNode mapping = object.path("discriminator").path("mapping");
            if (mapping instanceof ObjectNode mappings) {
                for (String name : List.copyOf(mappings.propertyNames())) {
                    mappings.put(name, renamedReference(mappings.get(name).asString(), references));
                }
            }
        }
        node.forEach(child -> rewrite(child, references, operationIds));
    }

    private String renamedReference(String reference, Map<String, String> references) {
        for (var entry : references.entrySet()) {
            if (reference.equals(entry.getKey()) || reference.startsWith(entry.getKey() + "/")) {
                return entry.getValue() + reference.substring(entry.getKey().length());
            }
        }
        return reference;
    }

    private void validateReferences(JsonNode node, JsonNode root) {
        if (node.path("$ref").isString()) {
            String reference = node.get("$ref").asString();
            if (reference.startsWith("#/") && root.at(reference.substring(1)).isMissingNode()) {
                throw new IllegalArgumentException("Unresolved OpenAPI reference: " + reference);
            }
        }
        node.forEach(child -> validateReferences(child, root));
    }

    private String pointer(String name) {
        return name.replace("~", "~0").replace("/", "~1");
    }
}
