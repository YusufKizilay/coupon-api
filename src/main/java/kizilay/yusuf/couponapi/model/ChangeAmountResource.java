package kizilay.yusuf.couponapi.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChangeAmountResource extends BaseResource {

    private double changedAmount;

    public ChangeAmountResource(double changedAmount) {
        this.changedAmount = changedAmount;
    }
}
