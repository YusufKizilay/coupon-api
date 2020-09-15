package kizilay.yusuf.couponapi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Coupon implements Serializable {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long couponId;

    @Column(name = "USER_ID")
    private Long userId;

    @Enumerated(EnumType.STRING)
    private CouponStatus status;

    private double cost;

    @Column(name = "PLAY_DATE")
    @JsonFormat(pattern = "YYYY-MM-dd HH:mm")
    private Date playDate;

    @Column(name = "CREATE_DATE")
    @CreatedDate
    @JsonFormat(pattern = "YYYY-MM-dd HH:mm")
    private Date createdDate;

    @Column(name = "UPDATE_DATE")
    @LastModifiedDate
    @JsonFormat(pattern = "YYYY-MM-dd HH:mm")
    private Date updatedDate;

    public Coupon(CouponStatus status, double cost, Set<Event> events) {
        this.status = status;
        this.cost = cost;
        this.events = events;
    }

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(
            name = "COUPON_SELECTION",
            joinColumns = @JoinColumn(name = "COUPON_ID"),
            inverseJoinColumns = @JoinColumn(name = "EVENT_ID"))
    private Set<Event> events;

}
