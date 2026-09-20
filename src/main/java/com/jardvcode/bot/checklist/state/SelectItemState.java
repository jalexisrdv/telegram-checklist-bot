package com.jardvcode.bot.checklist.state;

import com.jardvcode.bot.checklist.domain.Emoji;
import com.jardvcode.bot.checklist.dto.AssignmentDTO;
import com.jardvcode.bot.checklist.dto.SectionDTO;
import com.jardvcode.bot.checklist.dto.ItemDTO;
import com.jardvcode.bot.checklist.entity.ResponseEntity;
import com.jardvcode.bot.checklist.service.ResponseService;
import com.jardvcode.bot.shared.domain.bot.BotContext;
import com.jardvcode.bot.shared.domain.state.Decision;
import com.jardvcode.bot.shared.domain.state.State;
import com.jardvcode.bot.user.service.BotSessionDataService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public final class SelectItemState implements State {

    private final BotSessionDataService sessionDataService;
    private final ResponseService responseService;

    public SelectItemState(BotSessionDataService sessionDataService, ResponseService itemService) {
        this.sessionDataService = sessionDataService;
        this.responseService = itemService;
    }

    @Override
    public void onBotMessage(BotContext botContext) throws Exception {
        SectionDTO sectionDTO = sessionDataService.findByBotUserId(botContext.getBotUserId(), SectionDTO.class);
        AssignmentDTO assignmentDTO = sectionDTO.assignmentDTO();

        List<ResponseEntity> responses = responseService.findByAssignmentIdAndSectionId(assignmentDTO.assignmentId(), sectionDTO.id());

        StringBuilder message = new StringBuilder();

        message.append(String.format(
                "%s %s%n%n",
                Emoji.GROUP, StringUtils.capitalize(sectionDTO.name().toLowerCase())
        ));

        for (ResponseEntity response : responses) {
            message.append(String.format(
                    "%s %d. %s%n" +
                    "   %s%n%n",
                    response.status().emoji(), response.optionNumber(), StringUtils.capitalize(response.getItem().getLabel().toLowerCase()),
                    response.value()
            ));
        }

        botContext.sendText(message.toString());
    }

    @Override
    public Decision onUserInput(BotContext botContext) throws Exception {
        ResponseEntity response = null;

        try {
            Long optionNumber = Long.valueOf(botContext.getMessage());

            SectionDTO sectionDTO = sessionDataService.findByBotUserId(botContext.getBotUserId(), SectionDTO.class);
            AssignmentDTO assignmentDTO = sectionDTO.assignmentDTO();

            response = responseService.findByAssignmentIdAndSectionIdAndOptionNumber(assignmentDTO.assignmentId(), sectionDTO.id(), optionNumber);
        } catch(Exception e) {
            botContext.sendText("Opción no valida");

            return Decision.stay();
        }

        ItemDTO itemDTO = new ItemDTO(response.getId(), response.getItem().getLabel(), response.getAssignmentId());

        sessionDataService.save(botContext.getBotUserId(), itemDTO, getClass());

        return Decision.moveTo(AnswerItemState.class);
    }
}
