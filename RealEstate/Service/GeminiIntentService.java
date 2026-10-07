package com.project.RealEstate.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.project.RealEstate.Repository.PropertyRepository;

@Service
public class GeminiIntentService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonNode extractIntent(String userMessage) {

        ObjectNode response = mapper.createObjectNode();
        ObjectNode filters = emptyFilters();

        response.put("intent", "out_of_scope");
        response.set("filters", filters);
        response.put("message", "Sorry, I couldn't understand. Please try again.");

        try {

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key="
                    + apiKey;

            Map<String, Object> body = Map.of(
                    "contents", new Object[] {
                            Map.of("parts", new Object[] {
                                    Map.of("text", buildPrompt(userMessage))
                            })
                    });

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<String> res = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(body, headers),
                    String.class);

            JsonNode root = mapper.readTree(res.getBody());

            String text = root
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText("");

            JsonNode parsed = parseJson(text);

            if (parsed != null && parsed.isObject()) {

                String intent = parsed.has("intent")
                        ? parsed.get("intent").asText()
                        : "out_of_scope";
                response.put("intent", intent);

                // Allowed intents
                if ("search".equalsIgnoreCase(intent) ||
                        "greeting".equalsIgnoreCase(intent) ||
                        "general".equalsIgnoreCase(intent) ||
                        "out_of_scope".equalsIgnoreCase(intent)) {

                    if (parsed.hasNonNull("message")) {
                        response.put("message", parsed.get("message").asText());
                    }

                    if (parsed.has("filters") && parsed.get("filters").isObject()) {
                        filters.setAll((ObjectNode) parsed.get("filters"));
                    }
                }

            } else if (text != null && !text.trim().isEmpty()) {

                String lower = text.toLowerCase();

                if (lower.matches(".*\\b(hi|hello|hey|greetings)\\b.*")) {
                    response.put("message", "Hello! I am ready to help you find your dream property.");
                } else {
                    response.put("message", "I can only give responses based on real estate questions or property related inputs.");
                }
            }

        } catch (Exception e) {
            System.out.println("------------------------------------");
            System.out.println();
            System.err.println("Gemini Error: " + e.getMessage());
            System.out.println();
            System.out.println("------------------------------------");
            regexFallback(filters, userMessage);

            // Check if fallback found anything
            boolean foundSomething = filters.hasNonNull("location") || filters.hasNonNull("budget")
                    || filters.hasNonNull("propertyType") || filters.hasNonNull("bhk")
                    || filters.hasNonNull("purpose");

            System.out.println("-------------------------------------");
            System.out.println("Found something: " + foundSomething);
            System.out.println("-------------------------------------");

            String lower = userMessage.toLowerCase();

            if (foundSomething) {
                // Do nothing, let controller handle found/not found
            } else if (lower.matches(".*\\b(hi|hello|hey|greetings)\\b.*")) {
                response.put("intent", "greeting");
                response.put("message", "Hello! I am ready to help you find your dream property.");
            } else {
                response.put("intent", "out_of_scope");
                response.put("message", "I can only help you with property related questions...");
            }
        }

        sanitizeIntent(filters);

        // ------------------ STRICT PURPOSE EXTRACTION ------------------

        String lower = userMessage.toLowerCase();

        boolean buyMentioned = lower.contains("buy") ||
                lower.contains("purchase") ||
                lower.contains("own") ||
                lower.contains("investment");

        boolean rentMentioned = lower.contains("rent") ||
                lower.contains("lease") ||
                lower.contains("monthly") ||
                lower.contains("per month");

        if (!buyMentioned && !rentMentioned) {
            filters.putNull("purpose");
        }

        System.out.println("-------------------------------------");
        System.out.println("Filter : " + filters);
        System.out.println("-------------------------------------");
        return response;
    }

    @Autowired
    private PropertyRepository propertyRepository;

    public List<String> getSuggestions(String partialInput) {
        if (partialInput == null || partialInput.trim().length() < 2) {
            return Collections.emptyList();
        }

        List<String> suggestions = new ArrayList<>();

        // 1. Try Gemini AI
        try {
            String prompt = """
                    You are an intelligent real estate search auto-suggestion engine.
                    Generate 5-8 short, relevant search suggestions for the user input: "%s"

                    Rules:
                    1. Return ONLY a JSON array of strings.
                    2. Suggestions should cover location, budget, BHK, and property type.
                    3. Suggestions should be relevant to the user input.
                    4. Examples: "2 BHK in Chennai", "Apartment under 50L", "Villa in Bangalore".
                    5. strict JSON format ["suggestion1", "suggestion2"]
                    """
                    .formatted(partialInput);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key="
                    + apiKey;

            Map<String, Object> body = Map.of(
                    "contents", new Object[] {
                            Map.of("parts", new Object[] {
                                    Map.of("text", prompt)
                            })
                    });

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<String> res = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
            JsonNode root = mapper.readTree(res.getBody());
            String text = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

            // Extract JSON array from text (it might have markdown code blocks)
            int start = text.indexOf("[");
            int end = text.lastIndexOf("]");
            if (start != -1 && end != -1) {
                String jsonArray = text.substring(start, end + 1);
                JsonNode arrayNode = mapper.readTree(jsonArray);
                if (arrayNode.isArray()) {
                    for (JsonNode node : arrayNode) {
                        suggestions.add(node.asText());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Gemini Suggestion Error: " + e.getMessage());
        }

        // 2. Fallback to DB if Gemini failed or returned nothing
        if (suggestions.isEmpty()) {
            System.out.println("Gemini failed/empty, using DB fallback for: " + partialInput);

            // Fetch distinct locations
            List<String> locations = propertyRepository.findDistinctLocations(partialInput);
            for (String loc : locations) {
                suggestions.add("Property in " + loc);
                suggestions.add("under 50 Lakhs in " + loc);
                suggestions.add("under 75 Lakhs in " + loc);
                suggestions.add("under 1 Crore in " + loc);
                suggestions.add("1 BHK in " + loc);
                suggestions.add("2 BHK in " + loc);
                suggestions.add("3 BHK in " + loc);
                suggestions.add("4 BHK in " + loc);
            }

            // Fetch distinct types
            List<String> types = propertyRepository.findDistinctPropertyTypes(partialInput);
            for (String type : types) {
                suggestions.add(type);
                suggestions.add(type + "in Chennai");
                suggestions.add(type + "in Salem");
                suggestions.add(type + " for sale");
                suggestions.add(type + " under 50 Lakhs");
                suggestions.add(type + " under 75 Lakhs");
                suggestions.add(type + " for rent");
                suggestions.add(type + " for lease");
                suggestions.add(type + " under 1 Crore");
                suggestions.add(type + " 2BHK");
                suggestions.add(type + " 3BHK");
                suggestions.add(type + " 4BHK");

            }

            // Generic Fallbacks if DB also empty
            if (suggestions.isEmpty()) {
                suggestions.add("Apartment in Chennai");
                suggestions.add("Apartment in Bangalore");
                suggestions.add("Apartment in Coimbatore");
                suggestions.add("Apartment in Madurai");
                suggestions.add("Apartment in Tirunelveli");
                suggestions.add("Apartment in Tiruchirappalli");
                suggestions.add("Villa in Bangalore");
                suggestions.add("Villa in Coimbatore");
                suggestions.add("Villa in Madurai");
                suggestions.add("Villa in Tirunelveli");
                suggestions.add("Villa in Tiruchirappalli");
                suggestions.add("Apartment in Salem");
                suggestions.add("Villa in Salem");
                suggestions.add("2 BHK Flat");
                suggestions.add("3 BHK Flat");
                suggestions.add("4 BHK Flat");
                suggestions.add("House for Rent");
                suggestions.add("House for Sale");
                suggestions.add("House for Lease");
                suggestions.add("House under 50 Lakhs");
                suggestions.add("House under 75 Lakhs");
                suggestions.add("House under 1 Crore");
            }
        }

        // Limit to 10
        return suggestions.stream().limit(20).toList();
    }

    // ---------- Sanitize ----------

    public void sanitizeIntent(ObjectNode node) {

        if (node.hasNonNull("budget") && node.get("budget").asDouble() <= 0)
            node.putNull("budget");

        if (node.hasNonNull("bhk") && node.get("bhk").asInt() <= 0)
            node.putNull("bhk");

        cleanString(node, "location");
        cleanString(node, "propertyType");
        cleanString(node, "purpose");
    }

    private void cleanString(ObjectNode node, String field) {
        if (!node.hasNonNull(field))
            return;

        String val = node.get(field).asText().trim();
        if (val.isEmpty() || val.equalsIgnoreCase("null"))
            node.putNull(field);
    }

    // ---------- Prompt ----------

    private String buildPrompt(String input) {

        return """
                You are an Intelligent Real Estate Assistant.

                Your job is to ALWAYS understand user intent and aggressively extract property filters
                even if the user gives incomplete, misspelled, slang, or indirect sentences.

                ====================================================
                INTENT CLASSIFICATION
                ====================================================

                Classify user input into ONLY one:

                1. "search"
                   - User is looking for property
                   - Asking price, budget, location, type, BHK, rent or buy
                   - Even indirect search like:
                     "cheap house"
                     "need home"
                     "flat under 50L"
                     "villa in chennai"
                     "2 bedroom rent"
                   - When user wants to find, buy, rent, or filter properties

                2. "greeting"
                   - hi, hello, hey, good morning, etc.

                3. "general"
                   - Real estate knowledge questions
                   - Property related explanation
                   - Meaning based questions
                   - Educational questions about real estate
                   - Examples:
                   - "what is real estate"
                   - "what is BHK"
                   - "what is villa"
                   - "explain property tax"
                   - "difference between flat and apartment"
                   - Real estate knowledge questions
                   - Property explanations
                   - Real estate educational queries
                   - Meaning of real estate terms

                4. "out_of_scope"
                   - Any topic not related to real estate or property
                   - Sports, cinema, politics, programming, science, etc.


                ====================================================
                EXTRACTION RULES (VERY IMPORTANT)
                ====================================================

                ALWAYS try to extract filters from ANY phrasing.

                -------- LOCATION EXTRACTION --------
                - Extract city, area, or place names.
                - Accept spelling mistakes or partial matches.
                - Examples:
                  "chn" → Chennai
                  "bangalore" → Bangalore
                  "salem" → Salem
                  "madurai" → Madurai
                  "hydrabad" → Hyderabad
                  "hyderabad" → Hyderabad
                  "hyd" → Hyderabad
                  "Hyderabad" → Hyderabad
                  "trichy" → Trichy
                  "tirupur" → Tirupur
                  "hosur" → Hosur
                  "ooty" → Ooty
                  "coimbatore" → Coimbatore
                  "banglore" → Bangalore
                  "coimbatore area" → Coimbatore



                -------- BUDGET EXTRACTION --------
                Convert ALL price formats into absolute Rupees.

                Examples:
                10L → 1000000
                50 lakh → 5000000
                1 crore → 10000000
                25k rent → 25000
                30 thousand → 30000
                under 40L → 4000000
                below 25k → 25000
                less than 60 lakh → 6000000
                budget 15L → 1500000

                If user says:
                - cheap / low cost /→ keep budget null
                - luxury / premium → keep budget null



                -------- PROPERTY TYPE EXTRACTION --------
                Detect from keywords:
                - apartment | flat | house | plot | land | studio | penthouse | home |APARTMENT|FLAT|HOUSE|PLOT|LAND|STUDIO|PENTHOUSE|HOME ->Apartment
                - villa | VILLA | Villa | VillA ->Villa



                -------- BHK EXTRACTION --------
                Detect variations like:
                - 2 BHK ->2
                - 3 bedroom ->3
                - two bedroom ->2
                - 1 rk ->1
                - 4 bhk flat ->4

                Convert to numeric value.


                -------- PURPOSE EXTRACTION --------

                VERY STRICT RULE:

                ONLY extract purpose if user EXPLICITLY mentions it.

                BUY keywords:
                buy, purchase, own, investment

                RENT keywords:
                rent, lease, monthly, per month

                DO NOT guess purpose.
                DO NOT assume property search means buy.
                If user does not clearly mention buy or rent → purpose must be null.


                ====================================================
                SMART UNDERSTANDING RULES
                ====================================================

                - Extract multiple filters from single sentence.
                - If user gives partial information, still classify as "search".
                - Never assume values not mentioned.
                - If uncertain → keep value null.
                - Ignore grammar errors and slang.
                - Always prioritize user meaning over exact words.
                - If user says "I want to buy a property" → purpose = "buy"
                - If user says "I want to rent a property" → purpose = "rent"
                - If user asks explanation about real estate terms,
                  classify as "general"
                - Only classify as "out_of_scope"
                  if topic is completely unrelated to property,
                  like sports, cinema, politics, programming, etc.



                ====================================================
                RESPONSE FORMAT
                ====================================================

                Return ONLY valid JSON:

                {
                  "intent": "search" | "greeting" | "general" | "out_of_scope",
                  "message": "Friendly human-like reply",
                  "filters": {
                    "location": string | null,
                    "budget": number | null,
                    "propertyType": string | null,
                    "bhk": number | null,
                    "purpose": "buy" | "rent" | null
                  }
                }

                ====================================================
                USER INPUT
                ====================================================

                "%s"
                """
                .formatted(input);
    }

    // ---------- Helpers ----------

    private ObjectNode emptyFilters() {
        ObjectNode n = mapper.createObjectNode();
        n.putNull("location");
        n.putNull("budget");
        n.putNull("propertyType");
        n.putNull("bhk");
        n.putNull("purpose");
        return n;
    }

    private JsonNode parseJson(String text) {
        try {
            int s = text.indexOf("{");
            int e = text.lastIndexOf("}");
            if (s != -1 && e != -1) {
                return mapper.readTree(text.substring(s, e + 1));
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    // ---------- Offline Fallback ----------

    private void regexFallback(ObjectNode node, String msg) {

        String lower = msg.toLowerCase();

        // Locations (Expanded)
        if (lower.contains("chennai") || lower.contains("CHENNAI") || lower.contains("Chennai"))
            node.put("location", "Chennai");
        else if (lower.contains("salem") || lower.contains("selam") || lower.contains("salaem"))
            node.put("location", "Salem");
        else if (lower.contains("madurai") || lower.contains("MADURAI") || lower.contains("Madurai"))
            node.put("location", "Madurai");
        else if (lower.contains("coimbatore") || lower.contains("kovai") || lower.contains("COIMBATORE"))
            node.put("location", "Coimbatore");
        else if (lower.contains("trichy") || lower.contains("tiruchirappalli") || lower.contains("TRICHY")
                || lower.contains("Tiruchirappalli") || lower.contains("Trichy") || lower.contains("TIRUCHIRAPPALLI"))
            node.put("location", "Trichy");
        else if (lower.contains("bangalore") || lower.contains("bengaluru") || lower.contains("BANGALORE")
                || lower.contains("Bengaluru") || lower.contains("Bangalore") || lower.contains("BENGALURU"))
            node.put("location", "Bangalore");
        else if (lower.contains("erode") || lower.contains("ERODE") || lower.contains("Erode")
                || lower.contains("ERODAI") || lower.contains("Erodai") || lower.contains("Erodai"))
            node.put("location", "Erode");
        else if (lower.contains("tirupur") || lower.contains("TIRUPUR") || lower.contains("Tirupur"))
            node.put("location", "Tirupur");
        else if (lower.contains("hosur") || lower.contains("HOSUR") || lower.contains("Hosur"))
            node.put("location", "Hosur");
        else if (lower.contains("ooty") || lower.contains("OOTY") || lower.contains("Ooty"))
            node.put("location", "Ooty");
        else if (lower.contains("hyderabad") || lower.contains("HYDERABAD") || lower.contains("Hyderabad"))
            node.put("location", "Hyderabad");

        // BHK
        Matcher bhk = Pattern.compile("(\\d+)\\s*bhk").matcher(lower);
        if (bhk.find())
            node.put("bhk", Integer.parseInt(bhk.group(1)));

        // Budget
        Matcher lakh = Pattern.compile("(\\d+(\\.\\d+)?)\\s*lakh").matcher(lower);
        if (lakh.find()) {
            node.put("budget", Double.parseDouble(lakh.group(1)) * 100000);
        } else {
            Matcher crore = Pattern.compile("(\\d+(\\.\\d+)?)\\s*cr").matcher(lower);
            if (crore.find())
                node.put("budget", Double.parseDouble(crore.group(1)) * 10000000);
            else {
                Matcher k = Pattern.compile("(\\d+)\\s*k").matcher(lower);
                if (k.find())
                    node.put("budget", Double.parseDouble(k.group(1)) * 1000);
                else {
                    // Raw number (e.g. 5000000)
                    Matcher raw = Pattern.compile("(\\d{4,})").matcher(lower);
                    if (raw.find()) {
                        node.put("budget", Double.parseDouble(raw.group(1)));
                    }
                }
            }
        }

        // Property Type
        if (lower.contains("villa"))
            node.put("propertyType", "Villa");
        else if (lower.contains("apartment") || lower.contains("flat"))
            node.put("propertyType", "Apartment");
        else if (lower.contains("land") || lower.contains("plot"))
            node.put("propertyType", "Land");
        else if (lower.contains("house") || lower.contains("home"))
            node.put("propertyType", "House");

        // Purpose
        if (lower.contains("rent") || lower.contains("lease"))
            node.put("purpose", "rent");
        else if (lower.contains("buy") || lower.contains("purchase") || lower.contains("sale"))
            node.put("purpose", "buy");
    }
}