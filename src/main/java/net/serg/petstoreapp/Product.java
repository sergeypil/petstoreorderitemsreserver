package net.serg.petstoreapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Product  {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("name")
    private String name;

    @JsonProperty("photoURL")
    private String photoURL;
}
