package com.jardvcode.bot.checklist.state;

import com.jardvcode.bot.checklist.domain.BotCommand;
import com.jardvcode.bot.checklist.dto.AssignmentDTO;
import com.jardvcode.bot.checklist.dto.AssignmentDTOMother;
import com.jardvcode.bot.checklist.dto.SectionDTO;
import com.jardvcode.bot.checklist.dto.SectionDTOMother;
import com.jardvcode.bot.checklist.entity.SectionViewEntity;
import com.jardvcode.bot.checklist.entity.SectionViewEntityMother;
import com.jardvcode.bot.checklist.service.SectionService;
import com.jardvcode.bot.checklist.service.AssignmentService;
import com.jardvcode.bot.shared.domain.bot.BotContext;
import com.jardvcode.bot.shared.domain.exception.DataNotFoundException;
import com.jardvcode.bot.shared.domain.state.Decision;
import com.jardvcode.bot.user.service.BotSessionDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SelectSectionStateTest {

    @Mock
    private BotContext botContext;

    @Mock
    private BotSessionDataService sessionDataService;

    @Mock
    private SectionService sectionService;

    @Mock
    private AssignmentService instanceService;

    @InjectMocks
    private SelectSectionState state;

    @Test
    void shouldNotSendGroupsMessageWhenNoChecklistSelected() throws Exception {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Long mechanicUserId = 1L;

        when(botContext.getSystemUserId()).thenReturn(mechanicUserId);
        when(sessionDataService.findByBotUserId(mechanicUserId, AssignmentDTO.class)).thenThrow(DataNotFoundException.class);
        state.onBotMessage(botContext);

        verify(botContext, times(1)).sendText(captor.capture());
        assertEquals("Aún no has seleccionado una lista de inspección. Envía o pulsa " + BotCommand.ASSIGNMENTS.value() + " para ver las listas disponibles.", captor.getValue());
    }

    @Test
    void shouldSendGroupsMessageWhenChecklistSelected() throws Exception {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Long botUserId = 1L;
        AssignmentDTO dto = AssignmentDTOMother.create();
        List<SectionViewEntity> sections = SectionViewEntityMother.withRandomStatus();

        when(botContext.getBotUserId()).thenReturn(botUserId);
        when(sessionDataService.findByBotUserId(botUserId, AssignmentDTO.class)).thenReturn(dto);
        when(sectionService.findByAssignmentId(dto.assignmentId())).thenReturn(sections);
        state.onBotMessage(botContext);

        verify(botContext, times(1)).sendText(captor.capture());
        assertEquals(expectedMessageWithRandomStatus(), captor.getValue());
    }

    @Test
    void shouldSendInvalidOptionMessageWhenOptionDoesNotExist() throws Exception {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        AssignmentDTO dto = AssignmentDTOMother.create();
        Long optionNumber = 1L;

        when(sectionService.findByAssignmentIdAndOptionNumber(dto.assignmentId(), optionNumber)).thenThrow();
        Decision decision = state.onUserInput(botContext);

        verify(botContext).sendText(captor.capture());
        assertEquals("Opción no valida", captor.getValue());
        assertNull(decision.nextState());
    }

    @Test
    void shouldPersistGroupAndMoveToNextStateWhenOptionIsValid() throws Exception {
        String message = "1";
        Long botUserId = 1L;
        Long optionNumber = 1L;
        AssignmentDTO assignmentDTO = AssignmentDTOMother.create();
        SectionViewEntity section = SectionViewEntityMother.withPendingGroup();
        SectionDTO sectionDTO = SectionDTOMother.create();

        when(botContext.getMessage()).thenReturn(message);
        when(botContext.getBotUserId()).thenReturn(botUserId);
        when(sessionDataService.findByBotUserId(botUserId, AssignmentDTO.class)).thenReturn(assignmentDTO);
        when(sectionService.findByAssignmentIdAndOptionNumber(assignmentDTO.assignmentId(), optionNumber)).thenReturn(section);
        Decision decision = state.onUserInput(botContext);

        verify(sessionDataService, times(1)).save(botUserId, sectionDTO, SelectSectionState.class);
        assertEquals(SelectItemState.class, decision.nextState());
    }

    private String expectedMessageWithRandomStatus() {
        return String.format("""
                📂 Secciones
                
                ✅ 1. Sistema de dirección
                ⏳ 2. Suspensión delantera
                ✅ 3. Motor
                ⏳ 4. Embrague
                ✅ 5. Transmisión
                ⏳ 6. Diferenciales
                ✅ 7. Quintarueda
                ⏳ 8. Sistema eléctrico/electrónico
                ✅ 9. Sistema de rodamiento
                ⏳ 10. Sistema de frenos
                ✅ 11. Equipo de seguridad
                ⏳ 12. Limpieza y lubricación
                """, LocalDate.now().toString());
    }

}