package com.jardvcode.bot.checklist.state;

import com.jardvcode.bot.checklist.domain.BotCommand;
import com.jardvcode.bot.checklist.domain.Emoji;
import com.jardvcode.bot.checklist.dto.AssignmentDTO;
import com.jardvcode.bot.checklist.dto.SectionDTO;
import com.jardvcode.bot.checklist.entity.SectionViewEntity;
import com.jardvcode.bot.checklist.service.SectionService;
import com.jardvcode.bot.shared.domain.bot.BotContext;
import com.jardvcode.bot.shared.domain.state.Decision;
import com.jardvcode.bot.shared.domain.state.State;
import com.jardvcode.bot.user.service.BotSessionDataService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public final class SelectSectionState implements State {

    private final BotSessionDataService sessionDataService;
    private final SectionService sectionService;

    public SelectSectionState(SectionService sectionService, BotSessionDataService sessionDataService) {
        this.sectionService = sectionService;
        this.sessionDataService = sessionDataService;
    }

    @Override
    public void onBotMessage(BotContext botContext) throws Exception {
        AssignmentDTO assignmentDTO = null;

        try {
            assignmentDTO = sessionDataService.findByBotUserId(botContext.getBotUserId(), AssignmentDTO.class);
        } catch (Exception e) {
            botContext.sendText("Aún no has seleccionado una lista de inspección. Envía o pulsa " + BotCommand.ASSIGNMENTS.value() + " para ver las listas disponibles.");

            return;
        }

        StringBuilder message = new StringBuilder();

        message.append(String.format(
                "%s Secciones%n%n",
                Emoji.GROUP
        ));

        List<SectionViewEntity> sections = sectionService.findByAssignmentId(assignmentDTO.assignmentId());

        for (SectionViewEntity section : sections) {
            message.append(String.format(
                    "%s %d. %s%n",
                    section.getStatus().emoji(),
                    section.getOptionNumber(),
                    StringUtils.capitalize(section.getName().toLowerCase())
            ));
        }

        botContext.sendText(message.toString());
    }

    @Override
    public Decision onUserInput(BotContext botContext) throws Exception {
        AssignmentDTO assignmentDTO = null;
        SectionViewEntity section = null;

        try {
            Long optionNumber = Long.parseLong(botContext.getMessage());

            assignmentDTO = sessionDataService.findByBotUserId(botContext.getBotUserId(), AssignmentDTO.class);

            section = sectionService.findByAssignmentIdAndOptionNumber(assignmentDTO.assignmentId(), optionNumber);
        } catch (Exception e) {
            botContext.sendText("Opción no valida");

            return Decision.stay();
        }

        SectionDTO sectionDTO = new SectionDTO(section.getId(), section.getName(), assignmentDTO);

        sessionDataService.save(botContext.getBotUserId(), sectionDTO, getClass());

        return Decision.moveTo(SelectItemState.class);
    }
}
