package ru.homyakin.seeker.telegram.chat;

import java.time.LocalDateTime;
import ru.homyakin.seeker.locale.Language;

// TODO добавить дату добавления
public record Chat(
    long id,
    boolean isActive,
    Language language,
    LocalDateTime nextEventDate
) {
    public boolean isSameLanguage(Language language) {
        return this.language == language;
    }

    public Chat activate(ChatDao chatDao) {
        return changeActive(true, chatDao);
    }

    public Chat deactivate(ChatDao chatDao) {
        return changeActive(false, chatDao);
    }

    public Chat updateNextEventDate(LocalDateTime newNextEventDate, ChatDao chatDao) {
        final var chat = new Chat(
            id,
            isActive,
            language,
            newNextEventDate
        );
        chatDao.update(chat);
        return chat;
    }

    public Chat changeLanguage(Language newLanguage, ChatDao chatDao) {
        if (!isSameLanguage(newLanguage)) {
            final var chat = new Chat(
                id,
                isActive,
                newLanguage,
                nextEventDate
            );
            chatDao.update(chat);
            return chat;
        }
        return this;
    }

    private Chat changeActive(boolean newActive, ChatDao chatDao) {
        if (isActive != newActive) {
            final var chat = new Chat(
                id,
                newActive,
                language,
                nextEventDate
            );
            chatDao.update(chat);
            return chat;
        }
        return this;
    }
}
