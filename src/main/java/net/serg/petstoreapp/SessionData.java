package net.serg.petstoreapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SessionData {
    @JsonProperty("sessionId")
    private String sessionId;
    
    @JsonProperty("orderJson")
    private String orderJson;
}
