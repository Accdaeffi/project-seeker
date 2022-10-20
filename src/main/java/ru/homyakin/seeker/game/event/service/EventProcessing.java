package ru.homyakin.seeker.game.event.service;

import org.springframework.stereotype.Service;
import ru.homyakin.seeker.game.event.models.EventResult;
import ru.homyakin.seeker.game.event.models.LaunchedEvent;
import ru.homyakin.seeker.game.personage.PersonageService;

@Service
public class EventProcessing {
    private final EventService eventService;
    private final PersonageService personageService;
    private final BossProcessing bossProcessing;

    public EventProcessing(EventService eventService, PersonageService personageService, BossProcessing bossProcessing) {
        this.eventService = eventService;
        this.personageService = personageService;
        this.bossProcessing = bossProcessing;
    }

    public EventResult processEvent(LaunchedEvent launchedEvent) {
        final var event = eventService.getEventById(launchedEvent.eventId())
            .orElseThrow(() -> new IllegalStateException("Can't finish unknown event " + launchedEvent.eventId()));
        final var participants = personageService.getByLaunchedEvent(launchedEvent.id());

        return switch (event.type()) {
            case BOSS -> bossProcessing.process(event, participants);
        };
    }
}
