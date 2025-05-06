package com.spshpau.userservice.services;

import com.spshpau.userservice.dto.profiledto.SkillDto;
import com.spshpau.userservice.model.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SkillService {
    Skill createSkill(SkillDto skillDto); // Throws exception if name exists
    void deleteSkill(UUID skillId); // Throws exception if not found or deletion fails
    Page<Skill> getAllSkills(Pageable pageable); // Added pagination for consistency
}
