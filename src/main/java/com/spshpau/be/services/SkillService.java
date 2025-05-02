package com.spshpau.be.services;

import com.spshpau.be.dto.profiledto.SkillDto;
import com.spshpau.be.model.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SkillService {
    Skill createSkill(SkillDto skillDto); // Throws exception if name exists
    void deleteSkill(UUID skillId); // Throws exception if not found or deletion fails
    Page<Skill> getAllSkills(Pageable pageable); // Added pagination for consistency
}
