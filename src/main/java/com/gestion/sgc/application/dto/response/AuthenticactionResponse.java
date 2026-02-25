package com.gestion.sgc.application.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthenticactionResponse {

    private String token;

    private String nombre;

    @JsonProperty("token_type")
    @Builder.Default
    private String tokenType = "Bearer"; 

    @JsonProperty("expires_in")
    private Long expiresIn;


}
