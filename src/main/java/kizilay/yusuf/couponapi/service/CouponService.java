package kizilay.yusuf.couponapi.service;

import kizilay.yusuf.couponapi.entity.Coupon;
import kizilay.yusuf.couponapi.entity.CouponStatus;
import kizilay.yusuf.couponapi.entity.Event;
import kizilay.yusuf.couponapi.execption.CouponAlreadyExistException;
import kizilay.yusuf.couponapi.model.UserBalance;
import kizilay.yusuf.couponapi.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CouponService {

    private static final double COUPON_COST = 5.0;

    private CouponRepository couponRepository;
    private CouponValidator couponValidator;
    private LockService lockService;
    private OutgoingRestService outgoingRestService;

    @Autowired
    public CouponService(CouponRepository couponRepository, CouponValidator couponValidator,
                         LockService lockService, OutgoingRestService outgoingRestService) {
        this.couponRepository = couponRepository;
        this.couponValidator = couponValidator;
        this.lockService = lockService;
        this.outgoingRestService = outgoingRestService;
    }

    @Caching(evict = {
            @CacheEvict(value = "couponCacheWithId", allEntries = true),
            @CacheEvict(value = "couponCacheWithUserId", allEntries = true),
            @CacheEvict(value = "allCoupons", allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    public Coupon createCoupon(final Set<Long> eventIds) {
        List<Coupon> existedCoupons = findCoupons(null);

        Set<Event> events = couponValidator.validateCouponCreateProcess(eventIds,existedCoupons);

        Coupon coupon = new Coupon(CouponStatus.AVAILABLE, COUPON_COST, events);

        return couponRepository.save(coupon);
    }

    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "couponCacheWithId", allEntries = true),
            @CacheEvict(value = "couponCacheWithUserId", allEntries = true),
            @CacheEvict(value = "allCoupons", allEntries = true)
    })
    public Set<Coupon> buyCoupons(final Long userId, final Set<Coupon> coupons) {
        Set<Long> couponIds = coupons.stream().map(coupon -> coupon.getCouponId()).collect(Collectors.toSet());

        final Set<Coupon> soldCoupons = new HashSet<>();

        final Object lock = lockService.acquireLock(userId);

        //Eğer bu mikroservisten birden fazla instance olacak ise
        // Ya user bazında load balancing yapmak lazım (ki aynı user id için istekler aynı instance'a hit etsin)
        // Ya da local değil remote bir lock servis kullanılmalı

        synchronized (lock) {
            try {
                UserBalance userBalance = outgoingRestService.findUserBalance(userId);

                double availableBalance = userBalance.getBalance();

                for (Long couponId : couponIds) {

                    Coupon coupon = validateAndUpdateCoupon(userId, couponId, availableBalance);

                    availableBalance = availableBalance - coupon.getCost();

                    soldCoupons.add(coupon);
                }

                double changedAmount = availableBalance - userBalance.getBalance();

                outgoingRestService.updateUserBalance(userId, changedAmount);


            } finally {
                lockService.removeLock(userId);
            }

        }

        return soldCoupons;
    }

    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "couponCacheWithId", allEntries = true),
            @CacheEvict(value = "couponCacheWithUserId", allEntries = true),
            @CacheEvict(value = "allCoupons", allEntries = true)
    })
    public Coupon cancelCoupon(final Long userId, final Long couponId) {
        Coupon couponToBeCanceled = findCoupon(couponId);

        couponValidator.validateCouponCancelProcess(couponToBeCanceled, couponId, userId);

        couponToBeCanceled.setStatus(CouponStatus.CANCELED);

        final Object lock = lockService.acquireLock(userId);

        synchronized (lock) {
            try {
                outgoingRestService.updateUserBalance(userId, couponToBeCanceled.getCost());
            } finally {
                lockService.removeLock(userId);
            }

        }

        return couponToBeCanceled;
    }

    public List<Coupon> findCoupons(final CouponStatus couponStatus) {
        List<Coupon> coupons = couponRepository.findAll();

        if (null == couponStatus) {
            return coupons;
        }

        return coupons.stream().filter(coupon -> couponStatus.equals(coupon.getStatus())).collect(Collectors.toList());
    }

    public List<Coupon> findCouponsByUserId(final Long userId) {
        return couponRepository.findAllByUserId(userId);
    }

    private Coupon findCoupon(final Long couponId) {
        Optional<Coupon> coupon = couponRepository.findById(couponId);

        if (coupon.isPresent()) {
            return coupon.get();
        }
        return null;
    }

    private Coupon validateAndUpdateCoupon(final Long userId, final Long couponId, final double availableBalance) {
        Coupon coupon = findCoupon(couponId);


        couponValidator.validateCouponBuyProcess(coupon, couponId, availableBalance, userId);

        coupon.setUserId(userId);
        coupon.setStatus(CouponStatus.PLAYED);
        coupon.setPlayDate(Calendar.getInstance().getTime());

        return coupon;
    }

}
