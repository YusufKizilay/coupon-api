package kizilay.yusuf.couponapi.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserBalance extends BaseResource {

    private Long userId;

    private double balance;
}
