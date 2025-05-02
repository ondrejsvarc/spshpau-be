package com.spshpau.be.controller;

import com.spshpau.be.dto.profiledto.SkillDto;
import com.spshpau.be.model.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.UUID;

public interface SkillController {
    /** Create a new Skill */
    ResponseEntity<Skill> addSkill(@RequestBody SkillDto skillDto);

    /** Delete a Skill by ID */
    ResponseEntity<Void> deleteSkill(@PathVariable UUID skillId);

    /** Get all Skills with pagination */
    ResponseEntity<Page<Skill>> getAllSkills(Pageable pageable);
}
