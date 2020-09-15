package kizilay.yusuf.couponapi.repository;

import kizilay.yusuf.couponapi.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
}
