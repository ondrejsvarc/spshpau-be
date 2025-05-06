package com.spshpau.userservice.services.impl;

import com.spshpau.userservice.dto.profiledto.SkillDto;
import com.spshpau.userservice.model.Skill;
import com.spshpau.userservice.repositories.SkillRepository;
import com.spshpau.userservice.services.SkillService;
import com.spshpau.userservice.services.exceptions.DuplicateException;
import com.spshpau.userservice.services.exceptions.SkillNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;

    @Override
    @Transactional
    public Skill createSkill(SkillDto skillDto) {
        Optional<Skill> existingSkill = skillRepository.findByNameIgnoreCase(skillDto.getName());
        if (existingSkill.isPresent()) {
            throw new DuplicateException("Skill with name '" + skillDto.getName() + "' already exists.");
        }

        Skill newSkill = new Skill(skillDto.getName());
        Skill savedSkill = skillRepository.save(newSkill);
        return savedSkill;
    }

    @Override
    @Transactional
    public void deleteSkill(UUID skillId) {
        if (!skillRepository.existsById(skillId)) {
            throw new SkillNotFoundException("Skill not found with ID: " + skillId);
        }

        try {
            skillRepository.deleteById(skillId);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Cannot delete skill with ID " + skillId + " because it is currently assigned to one or more artist profiles.");
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Skill> getAllSkills(Pageable pageable) {
        return skillRepository.findAll(pageable);
    }
}
