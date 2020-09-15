package kizilay.yusuf.couponapi.service;

import kizilay.yusuf.couponapi.entity.Coupon;
import kizilay.yusuf.couponapi.entity.CouponStatus;
import kizilay.yusuf.couponapi.entity.Event;
import kizilay.yusuf.couponapi.entity.EventType;
import kizilay.yusuf.couponapi.execption.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;

@Service
public class CouponValidator {

    @Value("${MAX_COUPON_CANCEL_DURATION_MILIS}")
    private Long macCouponCancelDuration;

    private EventService eventService;

    public CouponValidator(EventService eventService) {
        this.eventService = eventService;
    }

    public Set<Event> validateCouponCreateProcess(List<Long> eventIds) {
        final int eventSize = eventIds.size();
        final AtomicReference<EventType> includedEventType = new AtomicReference<>();
        final Set<Event> events = new HashSet<>();

        eventIds.stream().forEach(eventId -> {
            Event event = eventService.findEvent(eventId);

            validateEvent(event, eventId);

            validateMbs(event.getMbs(), eventSize, eventId);

            validateEventDate(event.getEventDate(), eventId);

            validateEventsCompatibility(includedEventType, event.getEventType());

            events.add(event);

        });

        return events;
    }

    public void validateCouponBuyProcess(final Coupon coupon, final Long couponId, final double availableBalance, final Long userId) {
        validateCouponExist(coupon, couponId);

        if (CouponStatus.PLAYED.equals(coupon.getStatus())) {
            throw new CouponNotAvailableException(String.format("The coupon is not in required status! couponId: %d, Status: %s", couponId, coupon.getStatus()));
        }

        if (coupon.getCost() > availableBalance) {
            throw new UserBalanceNotAvailableException(String.format("User balance is not suitable for this operation! userId: %d", userId));
        }

    }


    public void validateCouponCancelProcess(final Coupon coupon, final Long couponId, final Long userIdInput) {
        validateCouponExist(coupon, couponId);

        if(!coupon.getUserId().equals(userIdInput)){
            throw new UnauthorizedCancelOperationException(String.format("The coupon does not belong to this user! couponId: %d, userId: %d", couponId, userIdInput));
        }

        if (!CouponStatus.PLAYED.equals(coupon.getStatus())) {
            throw new CouponNotAvailableException(String.format("The coupon is not in required status! couponId: %d, status: %s", couponId, coupon.getStatus()));
        }

        Date currentDate = Calendar.getInstance().getTime();

        long diff = Math.abs(currentDate.getTime() - coupon.getPlayDate().getTime());

        if (diff > macCouponCancelDuration) {
            throw new CouponCancelDurationExpiredException(String.format("The coupon can not be canceled. Duration is expired! couponId: %d", couponId));
        }
    }

    private void validateCouponExist(final Coupon coupon, final Long couponId) {
        if (null == coupon) {
            throw new CouponNotFoundException(String.format("Coupon is not found! couponId: %d", couponId));
        }
    }

    private void validateEvent(final Event event, final Long eventId) {
        if (null == event) {
            throw new EventNotFoundException(String.format("Event is not found! eventId: %d", eventId));
        }
    }

    private void validateMbs(final int eventMbsSize, final int eventSize, final Long eventId) {
        if (eventMbsSize > eventSize) {
            throw new NotEnoughEventException(String.format("Event size should be greater then mbs! eventId: %d , eventMbsSize: %d", eventId, eventMbsSize));
        }
    }

    private void validateEventDate(final Date eventDate, final Long eventId) {
        if (Calendar.getInstance().getTime().compareTo(eventDate) > 0) {
            throw new EventDateExpiredException(String.format("Event is expired! eventId: %d", eventId));
        }
    }

    private void validateEventsCompatibility(final AtomicReference<EventType> includedEventType, final EventType currentEventType) {
        BinaryOperator<EventType> accumulator = (includedEvent, currentEvent) -> {
            boolean preFootballCurrentTennis = null != includedEvent && includedEvent.equals(EventType.FOOTBALL) && currentEvent.equals(EventType.TENNIS);
            boolean preTennisCurrentFootball = null != includedEvent && includedEvent.equals(EventType.TENNIS) && currentEvent.equals(EventType.FOOTBALL);

            if (preFootballCurrentTennis || preTennisCurrentFootball) {
                throw new EventsAreIncompatibleException("A coupon should not have both tennis and football events.");
            }


            if ((null == includedEvent) || (currentEvent.equals(EventType.TENNIS) || currentEvent.equals(EventType.FOOTBALL))) {
                return currentEvent;
            } else {
                return includedEvent;
            }

        };

        includedEventType.getAndAccumulate(currentEventType, accumulator);
    }


}
