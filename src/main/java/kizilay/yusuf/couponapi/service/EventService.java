package kizilay.yusuf.couponapi.service;

import kizilay.yusuf.couponapi.entity.Event;
import kizilay.yusuf.couponapi.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private EventRepository eventRepository;

    @Autowired
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Caching(evict = {
            @CacheEvict(value = "allEvents", allEntries = true),
            @CacheEvict(value = "events", allEntries = true)
    })
    public List<Event> saveEvents(final List<Event> events) {
        return this.eventRepository.saveAll(events);
    }

    @Cacheable(value = "events", key = "#id")
    public Event findEvent(final Long id) {
        Optional<Event> event = eventRepository.findById(id);

        return event.isPresent() ? event.get() : null;
    }

    @Cacheable(value = "allEvents", key = "#root.method.name")
    public List<Event> findAllEvents() {
        return eventRepository.findAll();
    }
}
