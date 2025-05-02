package com.spshpau.be.dto.profiledto;

import com.spshpau.be.model.enums.ExperienceLevel;
import lombok.Data;

@Data
public class ProfileUpdateDto {
    private Boolean availability;
    private String bio;
    private ExperienceLevel experienceLevel;
}
