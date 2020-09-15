package kizilay.yusuf.couponapi.controller;

import kizilay.yusuf.couponapi.entity.Coupon;
import kizilay.yusuf.couponapi.entity.CouponStatus;
import kizilay.yusuf.couponapi.entity.Event;
import kizilay.yusuf.couponapi.model.ArrayOfCoupons;
import kizilay.yusuf.couponapi.model.ArrayOfEvents;
import kizilay.yusuf.couponapi.model.CouponResource;
import kizilay.yusuf.couponapi.model.Response;
import kizilay.yusuf.couponapi.service.CouponService;
import kizilay.yusuf.couponapi.util.ConverterUtil;
import kizilay.yusuf.couponapi.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class CouponController extends BaseController {

    private CouponService couponService;

    @Autowired
    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping("/coupons")
    public ResponseEntity<Response> createCoupon(@RequestBody ArrayOfEvents eventsResource) {
        List<Event> events = ConverterUtil.toEntity(eventsResource);

        Set<Long> eventIds = events.stream().map(event -> event.getEventId()).collect(Collectors.toSet());

        Coupon createdCoupon = couponService.createCoupon(eventIds);

        return ResponseUtil.successResponse(createdCoupon, HttpStatus.CREATED);
    }

    @GetMapping("/coupons")
    public ResponseEntity<Response> findCoupons(@RequestParam(required = false) CouponStatus status) {
        List<Coupon> coupons = couponService.findCoupons(status);

        if (!CollectionUtils.isEmpty(coupons)) {
            return ResponseUtil.successResponse(coupons, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/coupons/users/{userId}")
    public ResponseEntity<Response> findCouponsByUserId(@PathVariable Long userId) {
        List<Coupon> coupons = couponService.findCouponsByUserId(userId);

        if (!CollectionUtils.isEmpty(coupons)) {
            return ResponseUtil.successResponse(coupons, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/coupons/users/{userId}/buy")
    public ResponseEntity<Response> buyCoupons(@RequestBody ArrayOfCoupons arrayOfCoupons, @PathVariable Long userId) {

        Set<Coupon> soldCoupons = couponService.buyCoupons(userId, ConverterUtil.toEntity(arrayOfCoupons));

        return ResponseUtil.successResponse(soldCoupons, HttpStatus.OK);
    }

    @PutMapping("/coupons/users/{userId}/cancel")
    public ResponseEntity<Response> cancelCoupon(@RequestBody CouponResource couponResource, @PathVariable Long userId) {

        Coupon canceledCoupon = couponService.cancelCoupon(userId, ConverterUtil.toEntity(couponResource).getCouponId());

        return ResponseUtil.successResponse(canceledCoupon, HttpStatus.OK);
    }

}
