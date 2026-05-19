package com.uma.example.springuma.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ImagenAPIPredictor {
    //Class that is responsible for creating API call to external API service which will predict imagage.
    //Call in this version is based on the path of the image, but i checked if it works from our spring project

    private static final String API_URL = "https://api-inference.huggingface.co/models/MUmairAB/Breast_Cancer_Detector";
    // @TODO: replace USE_TOKEN_HERE with your token
    private static final String TOKEN = "Bearer USE_TOKEN_HERE";

    public static Map<String, Double> query(byte[] file_data) throws IOException, ApiException {

        byte[] data = file_data;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(API_URL);
            request.setHeader("Authorization", TOKEN);
            request.setEntity(new ByteArrayEntity(data));

            HttpResponse response = client.execute(request);
            String jsonResponse = EntityUtils.toString(response.getEntity());
            return processResponse(jsonResponse);
        } catch (IOException e) {
            // I/O level problems while performing request
            throw e;
        } catch (Exception e) {
            // Wrap other exceptions as ApiException to provide context
            throw new ApiException("Failed to query prediction API", e);
        }
    }

    private static Map<String, Double> processResponse(String jsonResponse) throws IOException, ApiException {
        ObjectMapper mapper = new ObjectMapper();
        if (jsonResponse == null) {
            throw new ApiException("Empty response from prediction API");
        }
        if (jsonResponse.trim().startsWith("[")) {
            List<Map<String, Object>> responseList = mapper.readValue(jsonResponse, new TypeReference<List<Map<String, Object>>>() {
            });
            Map<String, Double> resultMap = new HashMap<>();
            for (Map<String, Object> entry : responseList) {
                Object label = entry.get("label");
                Object score = entry.get("score");
                if (label instanceof String && score instanceof Number) {
                    resultMap.put((String) label, ((Number) score).doubleValue());
                }
            }
            return resultMap;
        } else {
            Map<String, Object> responseMap = mapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {
            });
            if (responseMap.containsKey("error")) {
                throw new ApiException("Error from API: " + responseMap.get("error"));
            } else {
                throw new ApiException("Unexpected response format: " + jsonResponse);
            }
        }
    }
}
