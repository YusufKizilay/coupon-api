package kizilay.yusuf.couponapi.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class ArrayOfCoupons extends BaseResource {

    private Set<CouponResource> coupons;

}
