package com.jardvcode.bot.user.state;

import com.jardvcode.bot.shared.domain.bot.BotContext;
import com.jardvcode.bot.shared.domain.state.Decision;
import com.jardvcode.bot.shared.domain.state.State;
import org.springframework.stereotype.Service;

@Service
public final class HelpState implements State {
    @Override
    public void onBotMessage(BotContext botContext) throws Exception {
        String message = """
                ¡Hola! 👋\s
                
                Bienvenido al sistema de inspecciones de mantenimiento:\s
                
                📋 /a – Ver y seleccionar una inspección asignada\s
                📂 /s – Ver secciones e ingresar a responder los puntos de la inspección seleccionada\s
                📄 /r – Ver el avance y resumen de la inspección seleccionada\s
                
                ¡Presiona o escribe /a para elegir tu inspección y comenzar! 🚗🔧
                """;

        botContext.sendText(message);
    }

    @Override
    public Decision onUserInput(BotContext botContext) throws Exception {
        return Decision.moveTo(getClass());
    }
}
