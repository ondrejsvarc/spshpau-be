package com.spshpau.be.dto.profiledto;

import com.spshpau.be.model.enums.ExperienceLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProducerProfileSummaryDto {
    private boolean availability;
    private ExperienceLevel experienceLevel;
}
