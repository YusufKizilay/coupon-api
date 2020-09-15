package kizilay.yusuf.couponapi.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@Configuration
/**
 * Enable'lı annotationlar için {@link kizilay.yusuf.couponapi.CouponApiApplication}'i kullanmak iyi bir fikir değil.
 * Testte sorun çıkartıyor. Boş bile olsa ayrı config class daha iyi.
 */
@EnableJpaAuditing
@EnableCaching
public class AppConfig {
}
