package com.jardvcode.bot.checklist.state;

import com.jardvcode.bot.checklist.dto.ItemDTO;
import com.jardvcode.bot.checklist.service.AssignmentService;
import com.jardvcode.bot.checklist.service.ResponseService;
import com.jardvcode.bot.checklist.service.SectionService;
import com.jardvcode.bot.shared.domain.bot.BotContext;
import com.jardvcode.bot.shared.domain.state.Decision;
import com.jardvcode.bot.shared.domain.state.State;
import com.jardvcode.bot.user.service.BotSessionDataService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public final class AnswerItemState implements State {

    private final BotSessionDataService sessionDataService;
    private final AssignmentService assignmentService;
    private final SectionService sectionService;
    private final ResponseService responseService;

    public AnswerItemState(BotSessionDataService sessionDataService, AssignmentService assignmentService, SectionService sectionService, ResponseService responseService) {
        this.sessionDataService = sessionDataService;
        this.assignmentService = assignmentService;
        this.sectionService = sectionService;
        this.responseService = responseService;
    }

    @Override
    public void onBotMessage(BotContext botContext) throws Exception {
        ItemDTO itemDTO = sessionDataService.findByBotUserId(botContext.getBotUserId(), ItemDTO.class);

        botContext.sendText("Envía el estatus de " + itemDTO.label().toLowerCase());
    }

    @Override
    public Decision onUserInput(BotContext botContext) throws Exception {
        String response = botContext.getMessage();

        Pattern pattern = Pattern.compile("^\s*(R|F|OK)(\s(.*))?$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(response);

        if (!matcher.matches()) {
            String message = """
                    Formato no válido. Responda únicamente con \"F\" (fallas), \"OK\" (en condiciones) o \"R\" (se reparó). Opcionalmente, agregue un espacio seguido de su comentario. Ejemplo: F comentario
                    """;
            botContext.sendText(message);

            return Decision.moveTo(getClass());
        }

        String status = matcher.group(1).trim();
        String comment = Optional.ofNullable(matcher.group(2)).orElse("").trim();

        ItemDTO itemDTO = sessionDataService.findByBotUserId(botContext.getBotUserId(), ItemDTO.class);

        responseService.save(itemDTO.id(), status, comment);

        assignmentService.updateStatus(itemDTO.assignmentId());
        sectionService.updateStatus(itemDTO.assignmentId());

        return Decision.moveTo(SelectItemState.class);
    }
}
