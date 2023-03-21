package ru.homyakin.seeker.telegram.command.group.language;

import org.springframework.stereotype.Component;
import ru.homyakin.seeker.locale.common.CommonLocalization;
import ru.homyakin.seeker.telegram.group.GroupService;
import ru.homyakin.seeker.telegram.command.CommandExecutor;
import ru.homyakin.seeker.telegram.TelegramSender;
import ru.homyakin.seeker.telegram.utils.InlineKeyboards;
import ru.homyakin.seeker.telegram.utils.TelegramMethods;

@Component
public class GroupSelectLanguageExecutor extends CommandExecutor<GroupSelectLanguage> {
    private final GroupService groupService;
    private final TelegramSender telegramSender;

    public GroupSelectLanguageExecutor(
        GroupService groupService,
        TelegramSender telegramSender
    ) {
        this.groupService = groupService;
        this.telegramSender = telegramSender;
    }

    @Override
    public void execute(GroupSelectLanguage command) {
        final var group = groupService.getOrCreate(command.groupId());
        final var updatedGroup = groupService.changeLanguage(group, command.language());
        telegramSender.send(
            TelegramMethods.createEditMessageText(
                command.groupId(),
                command.messageId(),
                CommonLocalization.chooseLanguage(updatedGroup.language()),
                InlineKeyboards.languageKeyboard(updatedGroup.language())
            )
        );
    }

}

