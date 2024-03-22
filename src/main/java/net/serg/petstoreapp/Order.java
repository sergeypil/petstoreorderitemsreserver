package net.serg.petstoreapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class Order implements Serializable {
    @JsonProperty("id")
    private String id;

    @JsonProperty("email")
    private String email;

    @JsonProperty("products")
    private List<Product> products;

    @JsonProperty("shipDate")
    private OffsetDateTime shipDate;
    
    @JsonProperty("status")
    private StatusEnum status;
    
    @JsonProperty("complete")
    private boolean complete;
    
    public enum StatusEnum {
        PLACED("placed"),

        APPROVED("approved"),

        DELIVERED("delivered");

        private String value;

        StatusEnum(String value) {
            this.value = value;
        }
    }
      
}
