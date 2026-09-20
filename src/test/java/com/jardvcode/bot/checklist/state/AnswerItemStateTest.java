package com.jardvcode.bot.checklist.state;

import com.jardvcode.bot.checklist.dto.ItemDTO;
import com.jardvcode.bot.checklist.dto.ItemDTOMother;
import com.jardvcode.bot.checklist.service.AssignmentService;
import com.jardvcode.bot.checklist.service.ResponseService;
import com.jardvcode.bot.checklist.service.SectionService;
import com.jardvcode.bot.shared.domain.bot.BotContext;
import com.jardvcode.bot.shared.domain.state.Decision;
import com.jardvcode.bot.user.service.BotSessionDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnswerItemStateTest {

    @Mock
    private BotContext botContext;

    @Mock
    private BotSessionDataService sessionDataService;

    @Mock
    private AssignmentService assignmentService;

    @Mock
    private SectionService sectionService;

    @Mock
    private ResponseService responseService;

    @InjectMocks
    private AnswerItemState state;

    @Test
    void shouldSendStatusMessageWhenItemSelected() throws Exception {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Long botUserId = 1L;
        ItemDTO dto = ItemDTOMother.create();

        when(botContext.getBotUserId()).thenReturn(botUserId);
        when(sessionDataService.findByBotUserId(botUserId, ItemDTO.class)).thenReturn(dto);
        state.onBotMessage(botContext);

        verify(botContext, times(1)).sendText(captor.capture());
        assertEquals("Envía el estatus de " + dto.label().toLowerCase(), captor.getValue());
    }

    @Test
    void shouldSendInvalidFormatMessageWhenInputIsIncorrect() throws Exception {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        String message = "asdad";

        when(botContext.getMessage()).thenReturn(message);
        Decision decision = state.onUserInput(botContext);

        verify(botContext, times(1)).sendText(captor.capture());
        assertEquals(expectedInvalidFormatMessage(), captor.getValue());
        assertEquals(AnswerItemState.class, decision.nextState());
    }

    @Test
    void shouldPersistStatusWhenInputIsCorrect() throws Exception {
        String message = "ok en condiciones";
        Long botUserId = 1L;
        ItemDTO dto = ItemDTOMother.create();
        String status = "ok";
        String comment = "en condiciones";

        when(botContext.getMessage()).thenReturn(message);
        when(botContext.getBotUserId()).thenReturn(botUserId);
        when(sessionDataService.findByBotUserId(botUserId, ItemDTO.class)).thenReturn(dto);
        Decision decision = state.onUserInput(botContext);

        verify(assignmentService, times(1)).updateStatus(dto.assignmentId());
        verify(sectionService, times(1)).updateStatus(dto.assignmentId());
        verify(responseService, times(1)).save(dto.id(), status, comment);
        assertEquals(SelectItemState.class, decision.nextState());
    }

    private String expectedInvalidFormatMessage() {
        return """
                Formato no válido. Responda únicamente con \"F\" (fallas), \"OK\" (en condiciones) o \"R\" (se reparó). Opcionalmente, agregue un espacio seguido de su comentario. Ejemplo: F comentario
                """;
    }

}