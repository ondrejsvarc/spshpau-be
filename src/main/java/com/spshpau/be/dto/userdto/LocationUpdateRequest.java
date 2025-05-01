package com.spshpau.be.dto.userdto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Lombok: Generates getters, setters, etc.
@NoArgsConstructor // Lombok: Needed for deserialization
public class LocationUpdateRequest {
    private String location;
}
