package kizilay.yusuf.couponapi.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ArrayOfEvents extends BaseResource {

    private List<EventResource> events;
}
