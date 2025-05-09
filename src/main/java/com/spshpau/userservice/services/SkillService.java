package com.spshpau.userservice.services;

import com.spshpau.userservice.dto.profiledto.SkillDto;
import com.spshpau.userservice.dto.profiledto.SkillSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SkillService {
    SkillSummaryDto createSkill(SkillDto skillDto);
    void deleteSkill(UUID skillId);
    Page<SkillSummaryDto> getAllSkills(Pageable pageable);
}
