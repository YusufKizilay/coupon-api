package kizilay.yusuf.couponapi.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import kizilay.yusuf.couponapi.entity.EventType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class EventResource extends BaseResource {
    private Long eventId;

    private String name;

    private Integer mbs;

    private EventType eventType;

    @JsonFormat(pattern = "YYYY-MM-dd HH:mm")
    private Date eventDate;
}
