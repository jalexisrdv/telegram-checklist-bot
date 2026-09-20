package com.jardvcode.bot.configuration.statemachine;

import com.jardvcode.bot.checklist.domain.BotCommand;
import com.jardvcode.bot.user.state.LinkBotToErpUserState;
import com.jardvcode.bot.shared.domain.bot.BotContext;
import com.jardvcode.bot.user.entity.BotUserEntity;
import com.jardvcode.bot.user.repository.BotUserStateRepository;
import com.jardvcode.bot.shared.domain.state.Decision;
import com.jardvcode.bot.shared.domain.state.State;
import com.jardvcode.bot.user.service.BotSessionDataService;
import org.springframework.stereotype.Service;

@Service
public final class StateMachine {

	private final BotUserStateRepository repository;
	private final StateRegistry stateRegistry;
	private final CommandRegistry commandRegistry;
	private final BotSessionDataService botSessionDataService;

	public StateMachine(BotUserStateRepository repository, StateRegistry stateRegistry, CommandRegistry commandRegistry, BotSessionDataService botSessionDataService) {
		this.repository = repository;
		this.stateRegistry = stateRegistry;
		this.commandRegistry = commandRegistry;
        this.botSessionDataService = botSessionDataService;
    }

	public void apply(BotContext botContext) throws Exception {
		String providerUserId = botContext.getProviderUserId();
		String message = botContext.getMessage();

		BotUserEntity botUser = repository.findWithRolesAndPermissionsByProviderUserId(providerUserId).orElse(null);

		if(botUser == null) {
			Class<? extends State> initialState = LinkBotToErpUserState.class;

			stateRegistry.find(initialState).onBotMessage(botContext);

			botUser = BotUserEntity.createWithoutErpUser(botContext.getPlatform(), providerUserId, initialState);
			repository.save(botUser);

			return;
		}

		botContext.setPreviousState(botUser.previousStateClass());
		botContext.setSystemUserId(botUser.getUserId());
		botContext.setBotUserId(botUser.getId());

		if(botUser.isErpUserLinked() && message.contains(BotCommand.ASSIGNMENTS.value())) {
			botSessionDataService.deleteByBotUserId(botUser.getUserId());
		}

		if(botUser.isErpUserLinked() && message.contains("/") && commandRegistry.canExecute(message, botUser.permissions())) {
			Class<? extends State> initialState = commandRegistry.find(message);
			stateRegistry.find(initialState).onBotMessage(botContext);

			botUser.updateCurrentState(initialState);
			repository.updateStateByProviderUserId(botUser.getPreviousState(), botUser.getCurrentState(), botUser.getProviderUserId());
			
			return;
		}

		Class<? extends State> currentState = botUser.currentStateClass();

		if(currentState == null) {
			return;
		}

		State currentStateFound = stateRegistry.find(currentState);

		if(currentStateFound == null) {
			return;
		}
		
		Decision newDecision = currentStateFound.onUserInput(botContext);
		Class<? extends State> nextState = newDecision.nextState();
				
		if(nextState == null) {
			return;
		}
		
		stateRegistry.find(nextState).onBotMessage(botContext);

		botUser.updateCurrentState(nextState);
		repository.updateStateByProviderUserId(botUser.getPreviousState(), botUser.getCurrentState(), botUser.getProviderUserId());
	}
}
