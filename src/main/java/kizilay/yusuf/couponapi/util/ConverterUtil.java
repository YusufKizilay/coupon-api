package kizilay.yusuf.couponapi.util;

import kizilay.yusuf.couponapi.entity.Coupon;
import kizilay.yusuf.couponapi.entity.Event;
import kizilay.yusuf.couponapi.model.ArrayOfCoupons;
import kizilay.yusuf.couponapi.model.ArrayOfEvents;
import kizilay.yusuf.couponapi.model.CouponResource;
import kizilay.yusuf.couponapi.model.EventResource;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ConverterUtil {

    private ConverterUtil() {
    }

    public static List<Event> toEntity(ArrayOfEvents arrayOfEvents) {
        List<EventResource> eventResources = arrayOfEvents.getEvents();

        return eventResources.stream().map(ConverterUtil::toEntity).collect(Collectors.toList());
    }

    public static Event toEntity(EventResource resource) {
        Event event = new Event();

        event.setEventId(resource.getEventId());
        event.setName(resource.getName());
        event.setEventDate(resource.getEventDate());
        event.setMbs(resource.getMbs());
        event.setEventType(resource.getEventType());

        return event;
    }

    public static Set<Coupon> toEntity(ArrayOfCoupons arrayOfCoupons) {
        Set<CouponResource> couponResources = arrayOfCoupons.getCoupons();

        return couponResources.stream().map(ConverterUtil::toEntity).collect(Collectors.toSet());
    }

    public static Coupon toEntity(CouponResource resource) {
        Coupon coupon = new Coupon();
        coupon.setCouponId(resource.getCouponId());
        return coupon;
    }
}
