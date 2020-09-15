package kizilay.yusuf.couponapi.controller;

import kizilay.yusuf.couponapi.entity.Event;
import kizilay.yusuf.couponapi.model.ArrayOfEvents;
import kizilay.yusuf.couponapi.model.Response;
import kizilay.yusuf.couponapi.service.EventService;
import kizilay.yusuf.couponapi.util.ConverterUtil;
import kizilay.yusuf.couponapi.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class EventController extends BaseController {

    private EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/events")
    public ResponseEntity<Response> saveEvents(@RequestBody ArrayOfEvents arrayOfEvents) {
        List<Event> savedEvents = eventService.saveEvents(ConverterUtil.toEntity(arrayOfEvents));

        return ResponseUtil.successResponse(savedEvents, HttpStatus.CREATED);
    }

    @GetMapping("/events")
    public ResponseEntity<Response> findAllEvents() {
        List events = eventService.findAllEvents();

        if (!CollectionUtils.isEmpty(events)) {
            return ResponseUtil.successResponse(events, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<Response> findEvent(@PathVariable Long id) {
        Event event = eventService.findEvent(id);

        if (null != event) {
            return ResponseUtil.successResponse(event, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);

    }

}
