package com.spshpau.userservice.controller;

import com.spshpau.userservice.dto.profiledto.SkillDto;
import com.spshpau.userservice.dto.profiledto.SkillSummaryDto;
import com.spshpau.userservice.model.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.UUID;

public interface SkillController {
    /** Create a new Skill */
    ResponseEntity<SkillSummaryDto> addSkill(@RequestBody SkillDto skillDto);

    /** Delete a Skill by ID */
    ResponseEntity<Void> deleteSkill(@PathVariable UUID skillId);

    /** Get all Skills with pagination */
    ResponseEntity<Page<SkillSummaryDto>> getAllSkills(Pageable pageable);
}
