package com.project.RealEstate.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.project.RealEstate.Entity.Property;
import com.project.RealEstate.Service.GeminiIntentService;
import com.project.RealEstate.Service.PropertyService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/chatbot")
public class ChatbotController {

    @Autowired
    private GeminiIntentService aiService;

    @Autowired
    private PropertyService propertyService;

    String userinput;

    @GetMapping("/suggestion")
    public ResponseEntity<List<String>> getSuggestions(@RequestParam("q") String query) {
        userinput = query;
        return ResponseEntity.ok(aiService.getSuggestions(query));
    }

    @PostMapping("/query")
    public ResponseEntity<?> chat(@RequestBody Map<String, String> req) {

        String msg = req.get("message").trim();

        System.out.println("-------------------------------------");
        System.out.println("User Message: " + msg);
        System.out.println("-------------------------------------");

        // ------------------ CALL AI ------------------
        JsonNode aiResponse = aiService.extractIntent(msg);

        String aiMessage = aiResponse.hasNonNull("message") ? aiResponse.get("message").asText() : null;
        System.out.println("-------------------------------------");
        System.out.println("AI Message: " + aiMessage);
        System.out.println("-------------------------------------");
        JsonNode filters = aiResponse.path("filters");
        if (filters != null && filters.isObject()) {
            aiService.sanitizeIntent((ObjectNode) filters);
        }

        String location = getText(filters, "location");
        Double budget = getDouble(filters, "budget");
        String type = getText(filters, "propertyType");
        Integer bhk = getInt(filters, "bhk");
        String purpose = getText(filters, "purpose");

        System.out.println("-------------------------------------");
        System.out.println("Location: " + location);
        System.out.println("Budget: " + budget);
        System.out.println("Property Type: " + type);
        System.out.println("BHK: " + bhk);
        System.out.println("Purpose: " + purpose);
        System.out.println("-------------------------------------");

        boolean hasFilters = location != null || budget != null || type != null || bhk != null || purpose != null;

        // ------------------ FETCH PROPERTIES FROM DB ------------------
        List<Property> list = new ArrayList<>();
        if (hasFilters) {
            list = propertyService.searchByIntent(location, budget, type, bhk, purpose);
        }

        // ------------------ BUILD RESPONSE ------------------
        Map<String, Object> res = new HashMap<>();

        // if (hasFilters) {
        // String statusMsg = !list.isEmpty()
        // ? "Property found for 😍: " + msg
        // : "Property not found for 😒: " + msg;
        // res.put("response", statusMsg);
        // } else {
        // // General Chat Response
        // if (aiMessage != null && !aiMessage.isEmpty()) {
        // System.out.println("gemini is responding : " + aiMessage);
        // if ("I can only help you with property related
        // questions...".equals(aiMessage)) {
        // res.put("response", aiMessage);
        // } else {
        // res.put("response", aiMessage);
        // }
        // } else {
        // res.put("response", "I didn't understand that. Could you try rephrasing?");
        // }
        // }

        if (hasFilters) {
            String statusMsg = !list.isEmpty()
                    ? "Property found for 😍: " + msg
                    : "Property not found for 😒: " + msg;
            res.put("response", statusMsg);
        } else {
            // 🔥 NEW ADDITION: If intent is search but no filters, return AI message
            String intent = aiResponse.hasNonNull("intent")
                    ? aiResponse.get("intent").asText()
                    : "out_of_scope";

            if ("out_of_scope".equals(intent)) {
                res.put("response",
                        "I can only give responses based on real estate questions or property related inputs.");
                return ResponseEntity.ok(res);
            }
            if ("greeting".equalsIgnoreCase(intent)) {
                res.put("response", aiMessage != null ? aiMessage : "Hello! 👋");
                return ResponseEntity.ok(res);
            }

            if ("search".equalsIgnoreCase(intent)) {
                res.put("response", aiMessage != null
                        ? aiMessage
                        : "Tell me location, budget or property type so I can help you better.");
            } else if ("general".equals(intent)) {
                res.put("response", (aiMessage != null && !aiMessage.isEmpty())
                        ? aiMessage
                        : "I didn't understand that. Could you try rephrasing?");
            }
            // else {
            // // existing general response
            // if (aiMessage != null && !aiMessage.isEmpty()) {
            // res.put("response", aiMessage);
            // } else {
            // res.put("response", "I didn't understand that. Could you try rephrasing?");
            // }
            // }
        }

        if (!list.isEmpty()) {
            List<Map<String, Object>> cards = new ArrayList<>();
            list.stream().forEach(p -> {
                if (p.getStatus().equals("ACTIVE")) {
                    System.out.println("Property found for User Input 😍: " + userinput + " --- Property Status : "
                            + p.getStatus());
                    Map<String, Object> c = new HashMap<>();
                    c.put("id", p.getId());
                    c.put("title", p.getTitle());
                    c.put("price", p.getPrice());
                    c.put("location", p.getLocation());
                    c.put("propertyType", p.getPropertyType());
                    c.put("bhk", p.getBhk());
                    c.put("status", p.getStatus());
                    c.put("purpose", p.getBuyRent());
                    cards.add(c);
                }
            });
            // res.put("response", "Found " + cards.size() + " properties for " +
            // userinput);
            res.put("count", cards.size());
            res.put("properties", cards);
        }

        return ResponseEntity.ok(res);
    }

    // ---------- Helpers ----------

    private String getText(JsonNode n, String f) {
        return n.hasNonNull(f) ? n.get(f).asText() : null;
    }

    private Double getDouble(JsonNode n, String f) {
        return n.hasNonNull(f) && n.get(f).isNumber()
                ? n.get(f).asDouble()
                : null;
    }

    private Integer getInt(JsonNode n, String f) {
        return n.hasNonNull(f) && n.get(f).isNumber()
                ? n.get(f).asInt()
                : null;
    }
}