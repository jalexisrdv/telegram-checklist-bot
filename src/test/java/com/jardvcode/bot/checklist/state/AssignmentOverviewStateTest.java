package com.jardvcode.bot.checklist.state;

import com.jardvcode.bot.checklist.domain.AssignmentOverviewMother;
import com.jardvcode.bot.checklist.domain.BotCommand;
import com.jardvcode.bot.checklist.domain.overview.AssignmentOverview;
import com.jardvcode.bot.checklist.dto.AssignmentDTO;
import com.jardvcode.bot.checklist.dto.AssignmentDTOMother;
import com.jardvcode.bot.checklist.service.AssignmentOverviewService;
import com.jardvcode.bot.shared.domain.bot.MessageAction;
import com.jardvcode.bot.shared.domain.bot.BotContext;
import com.jardvcode.bot.shared.domain.exception.DataNotFoundException;
import com.jardvcode.bot.user.service.BotSessionDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public final class AssignmentOverviewStateTest {

    @Mock
    private BotContext botContext;

    @Mock
    private BotSessionDataService sessionDataService;

    @Mock
    private AssignmentOverviewService service;

    @InjectMocks
    private AssignmentOverviewState state;

    @Test
    void shouldNotSendAssignmentOverviewMessageWhenNoChecklistSelected() throws Exception {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Long mechanicUserId = 1L;

        when(botContext.getSystemUserId()).thenReturn(mechanicUserId);
        when(sessionDataService.findByBotUserId(mechanicUserId, AssignmentDTO.class)).thenThrow(DataNotFoundException.class);
        state.onBotMessage(botContext);

        verify(botContext, times(1)).sendText(captor.capture());
        assertEquals("Aún no has seleccionado una lista de inspección. Envía o pulsa " + BotCommand.ASSIGNMENTS.value() + " para ver las listas disponibles.", captor.getValue());
    }

    @Test
    void shouldSendAssignmentOverviewWhenChecklistSelected() throws Exception {
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List> actionCaptor = ArgumentCaptor.forClass(List.class);

        Long botUserId = 1L;
        AssignmentDTO dto = AssignmentDTOMother.create();
        AssignmentOverview assignmentOverview = AssignmentOverviewMother.create();
        MessageAction expectedAction= new MessageAction("Continuar", "continue");

        when(botContext.getBotUserId()).thenReturn(botUserId);
        when(sessionDataService.findByBotUserId(botUserId, AssignmentDTO.class)).thenReturn(dto);
        when(service.getOverview(dto.assignmentId())).thenReturn(assignmentOverview);

        state.onBotMessage(botContext);

        verify(botContext, times(1)).sendActionMessage(messageCaptor.capture(), actionCaptor.capture());
        assertEquals(expectedMessage(), messageCaptor.getValue());
        assertEquals(expectedAction, actionCaptor.getValue().get(0));
    }

    private String expectedMessage() {
        return """
                📋 Lista de inspección: Formato para servicio D(COMPLETO)
                ⏳ Estatus: Pendiente
                📊 Progreso: 16/31 verificaciones completadas (51%)
                
                🚘 Código de unidad: 279
                🧑 Operador: José Alexis Ramírez del Valle
                🧑‍🔧 Mecánico: Juan Daniel Pérez Acosta
                📅 Fecha de asignación: 2026-08-27
                
                📂 Secciones
                
                ⏳ Sistema de dirección — 0/1 completados
                ⏳ Suspensión delantera — 0/1 completados
                ⏳ Motor — 5/6 completados
                ⏳ Embrague — 0/2 completados
                ✅ Transmisión — 3/3 completados
                ⏳ Diferenciales — 0/2 completados
                ⏳ Quintarueda — 0/2 completados
                ⏳ Sistema eléctrico/electrónico — 3/5 completados
                ⏳ Sistema de rodamiento — 0/2 completados
                ⏳ Sistema de frenos — 1/3 completados
                ✅ Equipo de seguridad — 2/2 completados
                ✅ Limpieza y lubricación — 2/2 completados
                """;
    }

}
