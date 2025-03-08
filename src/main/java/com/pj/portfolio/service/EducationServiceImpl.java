package com.pj.portfolio.service;

import com.pj.portfolio.dao.EducationRepository;
import com.pj.portfolio.entity.Education;
import com.pj.portfolio.service.interfaces.EducationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EducationServiceImpl implements EducationService {

    private EducationRepository educationRepository;

    @Autowired
    public EducationServiceImpl(EducationRepository educationRepository) {
        this.educationRepository = educationRepository;
    }

    @Override
    public List<Education> getAllEducationDetails() {
        return educationRepository.findAll();
    }

    @Override
    public Education findById(int theId) {
        educationRepository.findById(theId);
        Optional<Education> tempEducation = educationRepository.findById(theId);

        if(tempEducation.isPresent())
            return tempEducation.get();
        else
            throw new RuntimeException("Education doesn't exist in database");
    }

    @Override
    @Transactional
    public Education save(Education theEducation) {
        return educationRepository.save(theEducation);
    }

    @Override
    @Transactional
    public void deleteById(int theId) {
        educationRepository.deleteById(theId);
    }
}
