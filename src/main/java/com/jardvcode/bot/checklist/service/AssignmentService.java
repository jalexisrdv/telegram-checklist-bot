package com.jardvcode.bot.checklist.service;

import com.jardvcode.bot.checklist.domain.StatusEnum;
import com.jardvcode.bot.checklist.entity.AssignmentViewEntity;
import com.jardvcode.bot.checklist.repository.AssignmentRepository;
import com.jardvcode.bot.shared.domain.exception.DataNotFoundException;
import com.jardvcode.bot.shared.domain.exception.UnexpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public final class AssignmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssignmentService.class);

    private final AssignmentRepository repository;

    public AssignmentService(AssignmentRepository repository) {
        this.repository = repository;
    }

    public List<AssignmentViewEntity> findUnApprovedByMechanicUserId(Long mechanicUserId) {
        try {
            return repository.findByMechanicUserIdAndStatusNotOrderByDateDesc(mechanicUserId, StatusEnum.APPROVED);
        } catch (Exception e) {
            LOGGER.error("Unexpected error while retrieving assignments for mechanicUserId={}", mechanicUserId, e);
            throw new UnexpectedException();
        }
    }

    public AssignmentViewEntity findByMechanicUserIdAndOptionNumber(Long mechanicUserId, Long optionNumber) {
        try {
            return repository.findByMechanicUserIdAndOptionNumber(mechanicUserId, optionNumber).orElseThrow(() -> new DataNotFoundException());
        } catch (DataNotFoundException e) {
            LOGGER.error("Assignment not found for mechanicUserId={} optionNumber={}", mechanicUserId, optionNumber, e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unexpected error while retrieving assignment for mechanicUserId={} optionNumber={}", mechanicUserId, optionNumber, e);
            throw new UnexpectedException();
        }
    }

    public void updateStatus(Long assignmentId) {
        try {
            repository.updateStatus(assignmentId, StatusEnum.COMPLETED.name());
        } catch (Exception e) {
            LOGGER.error("Unexpected error while updating status for assignmentId={}", assignmentId, e);
            throw new UnexpectedException();
        }
    }

}
