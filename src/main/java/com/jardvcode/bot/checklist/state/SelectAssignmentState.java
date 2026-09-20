package com.jardvcode.bot.checklist.state;

import com.jardvcode.bot.checklist.dto.AssignmentDTO;
import com.jardvcode.bot.checklist.entity.AssignmentViewEntity;
import com.jardvcode.bot.checklist.service.AssignmentService;
import com.jardvcode.bot.shared.domain.bot.BotContext;
import com.jardvcode.bot.shared.domain.state.Decision;
import com.jardvcode.bot.shared.domain.state.State;
import com.jardvcode.bot.user.service.BotSessionDataService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public final class SelectAssignmentState implements State {

    private final BotSessionDataService sessionDataService;
    private final AssignmentService assignmentService;

    public SelectAssignmentState(BotSessionDataService sessionDataService, AssignmentService assignmentService) {
        this.sessionDataService = sessionDataService;
        this.assignmentService = assignmentService;
    }

    @Override
    public void onBotMessage(BotContext botContext) throws Exception {
        List<AssignmentViewEntity> assignments = assignmentService.findUnApprovedByMechanicUserId(botContext.getSystemUserId());

        if(assignments.isEmpty()) {
            botContext.sendText("¡Genial! No hay listas de inspección pendientes por responder.");

            return;
        }

        StringBuilder message = new StringBuilder();
        message.append("Estas son tus listas de inspección pendientes, envía el número de la lista que deseas responder:\n\n");

        for (AssignmentViewEntity assignment : assignments) {
            message.append(String.format(
                    "%s %d. %s%n" +
                            "   - Operador: %s%n" +
                            "   - Kilometraje: %s%n" +
                            "   - Próximo Servicio: %s%n" +
                            "   - Fecha: %s%n%n",
                    assignment.getStatus().emoji(),
                    assignment.getOptionNumber(),
                    assignment.getTemplateName(),
                    assignment.getOperatorFullName(),
                    assignment.getMileage(),
                    assignment.getNextService(),
                    assignment.getDate()
            ));

        }

        botContext.sendText(message.toString());
    }

    @Override
    public Decision onUserInput(BotContext botContext) throws Exception {
        AssignmentViewEntity assignment = null;

        try {
            Long userId = botContext.getSystemUserId();
            Long optionNumber = Long.parseLong(botContext.getMessage());

            assignment = assignmentService.findByMechanicUserIdAndOptionNumber(userId, optionNumber);
        } catch (Exception e) {
            botContext.sendText("Opción no valida");

            return Decision.stay();
        }

        AssignmentDTO assignmentDTO = new AssignmentDTO(
                assignment.getId(), assignment.getTemplateId(),
                assignment.getTemplateName(), assignment.getDate().toString(),
                assignment.getOperatorFullName(), assignment.getMileage(), assignment.getNextService()
        );

        sessionDataService.save(botContext.getBotUserId(), assignmentDTO, getClass());

        return Decision.moveTo(SelectSectionState.class);
    }
}
