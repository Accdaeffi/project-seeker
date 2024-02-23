package ru.homyakin.seeker.game.top.models;

import ru.homyakin.seeker.game.personage.badge.BadgeView;
import ru.homyakin.seeker.game.personage.models.PersonageId;
import ru.homyakin.seeker.locale.Language;
import ru.homyakin.seeker.locale.top.TopLocalization;

public record TopSpinPosition(
    PersonageId personageId,
    String personageName,
    BadgeView personageBadge,
    int workCount
) implements TopPosition {
    @Override
    public int score() {
        return workCount;
    }

    @Override
    public String toLocalizedString(Language language, int positionNumber) {
        return TopLocalization.topSpinPosition(language, positionNumber, this);
    }
}
