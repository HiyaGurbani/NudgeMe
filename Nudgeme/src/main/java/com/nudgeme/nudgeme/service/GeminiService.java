package com.nudgeme.nudgeme.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class GeminiService {

    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.key.host}")
    private String apiKeyHost;


    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    public String askGemini(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-goog-api-key", apiKey);

            LocalDate today = LocalDate.now(); // Java’s actual current date


            // Build request with Jackson
            ObjectMapper mapper = new ObjectMapper();

            ObjectNode textNode = mapper.createObjectNode();
            textNode.put("text",
                    "You are a friendly AI assistant helping the user manage goals and divide them into tasks. "
                            + "Be supportive and ask only one question at a time. "
                            + "Rules: "
                            + "- If the user gives a single clear goal (e.g. 'DSA'), accept it and move on. "
                            + "- If multiple goals, ask which one to start with. "
                            + "- If unclear or no goal, help brainstorm with simple examples. "
                            + "- After the goal is told, ask for priority. If the reply is unclear, set priority as 'Medium' by default. "
                            + "- After that, ask for end date or time range. If the user gives a time range in days or months .convert it into an end date accordingly "
                            + " you can use " + today +" as the current date. "
                            + "- Never go back to the goal once it’s clear. "
                            + "- Once goal, priority, and end date are set, create tasks for consecutive days starting today until the end date. "
                            + "- Present tasks in a clear numbered list without using bold (**). "
                            + "- If the number of tasks is large, only show 5–6 tasks as examples and let the user adjust. "
                            + "- After showing tasks, only ask one question: whether the user wants to adjust them (add, remove, update). "
                            + "- Wait for their reply before moving to the next step. "
                            + "- After tasks are finalized, Ask the user about a Mood Reminder that would help them stay consistent. "
                            + "- When asking about a Mood Reminder, only use one of the following moods: - Happy, Calm, Motivated, Focused, Relaxed, or Neutral. "
                            + "- Never invent new moods outside this list."
                            + "- Always return the mood as one of these exact words."
                            + "- Once everything is set, ask them if they want to save their plan. "
                            + "- Do not output JSON here — just continue the conversation naturally. "
                            + "JSON will be handled separately."
                            + "\n\nUser input: " + prompt

            );

            ObjectNode partsNode = mapper.createObjectNode();
            partsNode.set("parts", mapper.createArrayNode().add(textNode));

            ObjectNode userNode = mapper.createObjectNode();
            userNode.put("role", "user"); // ✅ Gemini expects role
            userNode.set("parts", partsNode.get("parts"));

            ObjectNode requestNode = mapper.createObjectNode();
            requestNode.set("contents", mapper.createArrayNode().add(userNode));

            String requestBody = mapper.writeValueAsString(requestNode);

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(GEMINI_URL, HttpMethod.POST, request, String.class);

            // Parse Gemini's response
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode textResponse = root.at("/candidates/0/content/parts/0/text");

            return (textResponse != null && !textResponse.isMissingNode())
                    ? textResponse.asText()
                    : "No response text from Gemini";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }


    public String finalizePlan(String conversation) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-goog-api-key", apiKey);

            ObjectMapper mapper = new ObjectMapper();

            // System message (strict schema)
            ObjectNode systemNode = mapper.createObjectNode();
            systemNode.put("role", "user");
            systemNode.set("parts", mapper.createArrayNode().add(
                    mapper.createObjectNode().put("text",
                            "You are a JSON formatter. Based on this conversation:\n\n" + conversation +
                                    "\n\nReturn ONLY valid JSON in this schema:\n" +
                                    "{ goal: { title, description, startDate, endDate, status, priority, category, moodId }, " +
                                    "tasks: [{ title, description, dueDate }] }" +
                                    "Return ONLY raw JSON. Do not include explanations, markdown formatting, or text outside the JSON object."
                    )
            ));

            ObjectNode requestNode = mapper.createObjectNode();
            requestNode.set("contents", mapper.createArrayNode().add(systemNode));

            String requestBody = mapper.writeValueAsString(requestNode);

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(GEMINI_URL, HttpMethod.POST, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode textResponse = root.at("/candidates/0/content/parts/0/text");

            return (textResponse != null && !textResponse.isMissingNode())
                    ? textResponse.asText()
                    : "No JSON generated";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }




    public String askHostGemini(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-goog-api-key", apiKeyHost);

            LocalDate today = LocalDate.now(); // Java’s actual current date


            // Build request with Jackson
            ObjectMapper mapper = new ObjectMapper();

            ObjectNode textNode = mapper.createObjectNode();
            textNode.put("text",
                    "You are an AI assistant that helps challenge hosts organize and promote challenges. "
                            + "Your style: casual, helpful, creative. "
                            + "Things you can do:\n"
                            + "- Suggest challenge themes that people usually like (fitness, learning, wellness, fun habits).\n"
                            + "- Give tips on how to attract more members to join challenges.\n"
                            + "- Suggest fun rewards or badges that motivate participants.\n"
                            + "- Share quick promotional ideas (like catchy titles, sharing strategies, or engaging descriptions).\n"
                            + "- Keep answers simple and friendly — like chatting with a co-host.\n"
                            + "- Always end with a follow-up question so the host keeps engaging.\n\n"
                            + "Host: " + prompt
            );

            ObjectNode partsNode = mapper.createObjectNode();
            partsNode.set("parts", mapper.createArrayNode().add(textNode));

            ObjectNode userNode = mapper.createObjectNode();
            userNode.put("role", "user"); // ✅ Gemini expects role
            userNode.set("parts", partsNode.get("parts"));

            ObjectNode requestNode = mapper.createObjectNode();
            requestNode.set("contents", mapper.createArrayNode().add(userNode));

            String requestBody = mapper.writeValueAsString(requestNode);

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(GEMINI_URL, HttpMethod.POST, request, String.class);

            // Parse Gemini's response
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode textResponse = root.at("/candidates/0/content/parts/0/text");

            return (textResponse != null && !textResponse.isMissingNode())
                    ? textResponse.asText()
                    : "No response text from Gemini";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }



}
