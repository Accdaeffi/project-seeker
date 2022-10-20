package ru.homyakin.seeker.game.personage.models;

import ru.homyakin.seeker.game.experience.ExperienceUtils;
import ru.homyakin.seeker.game.personage.PersonageDao;
import ru.homyakin.seeker.locale.Language;
import ru.homyakin.seeker.locale.Localization;

public record Personage(
    long id,
    int level,
    long currentExp
) {
    public Personage addExperience(long exp, PersonageDao personageDao) {
        final var newExp = currentExp + exp;
        var newLvl = level;
        if (newExp >= ExperienceUtils.getTotalExpToNextLevel(level)) {
            newLvl += 1;
        }
        final var personage = new Personage(
            id,
            newLvl,
            newExp
        );
        personageDao.update(personage);
        return personage;
    }

    public String toProfile(Language language) {
        return Localization
            .get(language)
            .profileTemplate()
            .formatted(level, currentExp, ExperienceUtils.getTotalExpToNextLevel(level));
    }
}
