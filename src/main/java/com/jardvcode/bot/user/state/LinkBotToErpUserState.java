package com.jardvcode.bot.user.state;

import com.jardvcode.bot.user.service.BotErpUserLinkService;
import com.jardvcode.bot.shared.domain.bot.BotContext;
import com.jardvcode.bot.shared.domain.exception.BotException;
import com.jardvcode.bot.shared.domain.state.Decision;
import com.jardvcode.bot.shared.domain.state.State;
import org.springframework.stereotype.Service;

@Service
public final class LinkBotToErpUserState implements State {

    private final BotErpUserLinkService service;

    public LinkBotToErpUserState(BotErpUserLinkService service) {
        this.service = service;
    }

    @Override
    public void onBotMessage(BotContext botContext) throws Exception {
        botContext.sendText("¡Hola! Para conectarte con tu cuenta, necesito que me envíes el token que recibiste.");
    }

    @Override
    public Decision onUserInput(BotContext botContext) throws Exception {
        String token = botContext.getMessage();

        try {
            service.linkBotToErpUser(token, botContext.getProviderUserId());
        } catch(BotException e) {
            botContext.sendText(e.getMessage());

            return Decision.moveTo(getClass());
        }

        return Decision.moveTo(HelpState.class);
    }
}
