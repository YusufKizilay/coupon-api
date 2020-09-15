package kizilay.yusuf.couponapi.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class LockService {

    private ConcurrentHashMap<Long, Object> map = new ConcurrentHashMap<>();

    public Object acquireLock(final Long userId) {
        map.putIfAbsent(userId, new Object());
        return map.get(userId);
    }

    public void removeLock(final Long userId){
        map.remove(userId);
    }
}
