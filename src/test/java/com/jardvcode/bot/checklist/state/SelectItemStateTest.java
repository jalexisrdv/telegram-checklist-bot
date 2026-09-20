package com.jardvcode.bot.checklist.state;

import com.jardvcode.bot.checklist.dto.*;
import com.jardvcode.bot.checklist.entity.ResponseEntity;
import com.jardvcode.bot.checklist.entity.ResponseEntityMother;
import com.jardvcode.bot.checklist.service.SectionService;
import com.jardvcode.bot.checklist.service.ResponseService;
import com.jardvcode.bot.shared.domain.bot.BotContext;
import com.jardvcode.bot.shared.domain.state.Decision;
import com.jardvcode.bot.user.service.BotSessionDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SelectItemStateTest {

    @Mock
    private BotContext botContext;

    @Mock
    private BotSessionDataService sessionDataService;

    @Mock
    private ResponseService responseService;

    @Mock
    private SectionService groupService;

    @InjectMocks
    private SelectItemState state;

    @Test
    void shouldSendItemsMessageWhenGroupSelected() throws Exception {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Long botUserId = 1L;
        SectionDTO dto = SectionDTOMother.create();
        ArrayList<ResponseEntity> responses = ResponseEntityMother.withSomeResponses();

        when(botContext.getBotUserId()).thenReturn(botUserId);
        when(sessionDataService.findByBotUserId(botUserId, SectionDTO.class)).thenReturn(dto);
        when(responseService.findByAssignmentIdAndSectionId(dto.assignmentDTO().assignmentId(), dto.id())).thenReturn(responses);
        state.onBotMessage(botContext);

        verify(botContext, times(1)).sendText(captor.capture());
        List<String> values = captor.getAllValues();
        assertEquals(expectedBodyMessageWithSomeResponses(), values.get(0));
    }

    @Test
    void shouldSendInvalidOptionMessageWhenOptionDoesNotExist() throws Exception {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        SectionDTO dto = SectionDTOMother.create();
        Long optionNumber = 1L;

        when(responseService.findByAssignmentIdAndSectionIdAndOptionNumber(dto.assignmentDTO().assignmentId(), dto.id(), optionNumber)).thenThrow();
        Decision decision = state.onUserInput(botContext);

        verify(botContext).sendText(captor.capture());
        assertEquals("Opción no valida", captor.getValue());
        assertNull(decision.nextState());
    }

    @Test
    void shouldPersistItemAndMoveToNextStateWhenOptionIsValid() throws Exception {
        String message = "1";
        Long botUserId = 1L;
        Long optionNumber = 1L;
        SectionDTO sectionDTO = SectionDTOMother.create();
        ResponseEntity response = ResponseEntityMother.withPendingItem();
        ItemDTO itemDTO = ItemDTOMother.create();

        when(botContext.getMessage()).thenReturn(message);
        when(botContext.getBotUserId()).thenReturn(botUserId);
        when(sessionDataService.findByBotUserId(botUserId, SectionDTO.class)).thenReturn(sectionDTO);
        when(responseService.findByAssignmentIdAndSectionIdAndOptionNumber(sectionDTO.assignmentDTO().assignmentId(), sectionDTO.id(), optionNumber)).thenReturn(response);
        Decision decision = state.onUserInput(botContext);

        verify(sessionDataService, times(1)).save(botUserId, itemDTO, SelectItemState.class);
        assertEquals(AnswerItemState.class, decision.nextState());
    }

    private String expectedBodyMessageWithSomeResponses() {
        return """
                📂 Sistema de dirección
                
                ✅ 1. Revisión de fugas (aceite, agua, diésel)
                   OK comentario
                
                ⏳ 2. Reset insite
                \s\s\s
                
                ✅ 3. Cambio de filtros (diésel)
                   OK comentario
                
                ⏳ 4. Revisar tensión de bandas
                \s\s\s
                
                ✅ 5. Nivel de refrigerante
                   OK comentario
                
                ⏳ 6. Cambiar filtro de aire según indicador de partículas
                \s\s\s
                
                """;
    }

}