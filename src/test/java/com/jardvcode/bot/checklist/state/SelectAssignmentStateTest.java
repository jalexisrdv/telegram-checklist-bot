package com.jardvcode.bot.checklist.state;

import com.jardvcode.bot.checklist.dto.AssignmentDTO;
import com.jardvcode.bot.checklist.dto.AssignmentDTOMother;
import com.jardvcode.bot.checklist.entity.AssignmentViewEntity;
import com.jardvcode.bot.checklist.entity.AssignmentViewEntityMother;
import com.jardvcode.bot.checklist.service.AssignmentService;
import com.jardvcode.bot.shared.domain.bot.BotContext;
import com.jardvcode.bot.shared.domain.state.Decision;
import com.jardvcode.bot.user.service.BotSessionDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SelectAssignmentStateTest {

    @Mock
    private BotContext botContext;

    @Mock
    private BotSessionDataService sessionDataService;

    @Mock
    private AssignmentService assignmentService;

    @InjectMocks
    private SelectAssignmentState state;

    @Test
    void shouldNotSendPendingChecklistMessageIfNoChecklistAssigned() throws Exception {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Long mechanicUserId = 1L;

        when(botContext.getSystemUserId()).thenReturn(mechanicUserId);
        when(assignmentService.findUnApprovedByMechanicUserId(mechanicUserId)).thenReturn(List.of());
        state.onBotMessage(botContext);

        verify(botContext, times(1)).sendText(captor.capture());
        assertEquals("¡Genial! No hay listas de inspección pendientes por responder.", captor.getValue());
    }

    @Test
    void shouldSendPendingChecklistMessageIfChecklistAssigned() throws Exception {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Long mechanicUserId = 1L;
        ArrayList<AssignmentViewEntity> assignments = AssignmentViewEntityMother.values();

        when(botContext.getSystemUserId()).thenReturn(mechanicUserId);
        when(assignmentService.findUnApprovedByMechanicUserId(mechanicUserId)).thenReturn(assignments);
        state.onBotMessage(botContext);

        verify(botContext, times(1)).sendText(captor.capture());
        assertEquals(getExpectedMessage(), captor.getValue());
    }

    @Test
    void shouldSendInvalidOptionMessageIfOptionDoesNotExist() throws Exception {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Long mechanicUserId = 1L;
        Long optionNumber = 1L;

        when(assignmentService.findByMechanicUserIdAndOptionNumber(mechanicUserId, optionNumber)).thenThrow();
        Decision decision = state.onUserInput(botContext);

        verify(botContext).sendText(captor.capture());
        assertEquals("Opción no valida", captor.getValue());
        assertNull(decision.nextState());
    }

    @Test
    void shouldPersistChecklistAndMoveToNextStateWhenOptionIsValid() throws Exception {
        String message = "1";
        Long mechanicUserId = 1L;
        Long optionNumber = 1L;
        AssignmentViewEntity assignment = AssignmentViewEntityMother.withPendingStatus();
        AssignmentDTO dto = AssignmentDTOMother.withInstance(assignment);

        when(botContext.getSystemUserId()).thenReturn(mechanicUserId);
        when(botContext.getMessage()).thenReturn(message);
        when(assignmentService.findByMechanicUserIdAndOptionNumber(mechanicUserId, optionNumber)).thenReturn(assignment);
        Decision decision = state.onUserInput(botContext);

        verify(sessionDataService, times(1)).save(mechanicUserId, dto, SelectAssignmentState.class);
        assertEquals(SelectSectionState.class, decision.nextState());
    }

    private String getExpectedMessage() {
        return String.format("""
                Estas son tus listas de inspección pendientes, envía el número de la lista que deseas responder:
                
                ⏳ 1. Formato para servicios A y C (BASICO)
                   - Operador: PEDRO OCELOT
                   - Kilometraje: 1299961
                   - Próximo Servicio: 1,300,000 BASICO
                   - Fecha: %s
                
                ✅ 1. Formato para servicios A y C (BASICO)
                   - Operador: PEDRO OCELOT
                   - Kilometraje: 1299961
                   - Próximo Servicio: 1,300,000 BASICO
                   - Fecha: %s
                   
                """, LocalDate.now().toString(), LocalDate.now().toString());
    }

}