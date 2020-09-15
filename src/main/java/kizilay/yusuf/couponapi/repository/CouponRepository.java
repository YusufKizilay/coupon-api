package kizilay.yusuf.couponapi.repository;

import kizilay.yusuf.couponapi.entity.Coupon;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    /**
     * @Cacheable annotation'ı service yerine repository'e koydum çünkü,
     * {@link kizilay.yusuf.couponapi.service.CouponService#buyCoupons(Long, Set)}
     * ve {@link kizilay.yusuf.couponapi.service.CouponService#cancelCoupon(Long, Long)}
     *
     * {@link kizilay.yusuf.couponapi.service.CouponService#findCoupon(Long)} 'u internal olarak çağırıyor.
     * Servis katmanındaki bu methoda @Cacheable annotation'ı koyarsam eğer aynı class içinden çağrıldığı için
     * çalışmayacaktı.
     *
     */

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Cacheable(value = "couponCacheWithId", key = "#couponId")
    Optional<Coupon> findById(Long couponId);

    @Cacheable(value = "couponCacheWithUserId", key = "#userId")
    List<Coupon> findAllByUserId(Long userId);

    @Override
    @Cacheable(value = "allCoupons", key = "#root.method.name")
    List<Coupon> findAll();
}
