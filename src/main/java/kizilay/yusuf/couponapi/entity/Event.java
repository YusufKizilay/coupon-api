package kizilay.yusuf.couponapi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Event implements Serializable {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long eventId;

    @Column(nullable = false)
    private String name;

    @Column(name = "MBS", nullable = false)
    private Integer mbs;

    @Column(name = "TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Column(name = "EVENT_DATE", nullable = false)
    @JsonFormat(pattern = "YYYY-MM-dd HH:mm")
    private Date eventDate;
}
