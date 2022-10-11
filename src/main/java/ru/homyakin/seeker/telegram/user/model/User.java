package ru.homyakin.seeker.telegram.user.model;

import ru.homyakin.seeker.locale.Language;
import ru.homyakin.seeker.telegram.user.UserDao;

//TODO перенести юзера и чат в телегу
public record User(
    long id,
    boolean isActivePrivateMessages,
    Language language,
    long characterId
) {
    public boolean isSameLanguage(Language newLanguage) {
        return language == newLanguage;
    }

    public User changeLanguage(Language newLanguage, UserDao userDao) {
        if (!isSameLanguage(newLanguage)) {
            final var user = new User(
                id,
                isActivePrivateMessages,
                newLanguage,
                characterId
            );
            userDao.update(user);
            return user;
        }
        return this;
    }

    public User activatePrivateMessages(UserDao userDao) {
        if (!isActivePrivateMessages) {
            final var user = new User(
                id,
                true,
                language,
                characterId
            );
            userDao.update(user);
            return user;
        }
        return this;
    }
}