package kizilay.yusuf.couponapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import kizilay.yusuf.couponapi.entity.Coupon;
import kizilay.yusuf.couponapi.entity.CouponStatus;
import kizilay.yusuf.couponapi.model.*;
import kizilay.yusuf.couponapi.service.OutgoingRestService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@Import(CouponApiIntegrationTest.TestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CouponApiIntegrationTest {

    private static final String API_URL = "/bilyoner/coupons";
    private static final String HOST = "http://localhost:";

    @LocalServerPort
    private int port;
    TestRestTemplate restTemplate = new TestRestTemplate();

    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CacheManager cacheManager;


    @TestConfiguration
    public static class TestConfig {

        @MockBean
        OutgoingRestService outgoingRestService;

    }

    @Before
    public void beforeEach() {
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
    }

    @Test
    @Sql("/insert_events.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "/clean_all.sql")
    public void createCoupon_Should_Return400_When_Mbs_NotValid() {
        //insert_events.sql-> Event 1 -> Mbs 3
        ArrayOfEvents events = prepareEventsRequestBody(1);

        ResponseEntity<Response> response = restTemplate.postForEntity(createURI(""), createHttpEntity(events), Response.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Event size should be greater then mbs! eventId: 1 , eventMbsSize: 3", response.getBody().getError());
    }

    @Test
    @Sql("/insert_events.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "/clean_all.sql")
    public void createCoupon_Should_Return400_When_EventDateExpired() {
        //insert_events.sql-> Event 7 -> Event Date 2019
        ArrayOfEvents events = prepareEventsRequestBody(7);

        ResponseEntity<Response> response = restTemplate.postForEntity(createURI(""), createHttpEntity(events), Response.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Event is expired! eventId: 7", response.getBody().getError());
    }

    @Test
    @Sql("/insert_events.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "/clean_all.sql")
    public void createCoupon_should_Return400_When_Contains_FootballAndTennis() {
        //insert_events.sql-> Event 1 -> Football
        //insert_events.sql-> Event 15 -> Tennis
        ArrayOfEvents events = prepareEventsRequestBody(1, 8, 15);

        ResponseEntity<Response> response = restTemplate.postForEntity(createURI(""), createHttpEntity(events), Response.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("A coupon should not have both tennis and football events.", response.getBody().getError());
    }

    @Test
    @Sql("/insert_events.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "/clean_all.sql")
    public void createCoupon_Should_Return201_When_Request_Valid() {
        //insert_events.sql-> Event 12 -> Basketball
        //insert_events.sql-> Event 15 -> Tennis
        ArrayOfEvents events = prepareEventsRequestBody(12, 15);

        ResponseEntity<Response> response = restTemplate.postForEntity(createURI(""), createHttpEntity(events), Response.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    @Sql("/insert_coupon.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "/clean_all.sql")
    public void buyCoupon_Should_Return200_When_Request_Valid() {
        OutgoingRestService mockOutgoingRestService = (OutgoingRestService) applicationContext.getBean("outgoingRestService");
        when(mockOutgoingRestService.findUserBalance(1L)).thenReturn(prepareUserBalanceResponse(1, 30.00));

        ArrayOfCoupons coupons = prepareCouponsRequestBody(1);
        ResponseEntity<Response> response = restTemplate.exchange(createURI("/users/1/buy"), HttpMethod.PUT, createHttpEntity(coupons), Response.class);

        List<Coupon> playedCoupons = deserialize(response.getBody().getSuccess(), new TypeReference<List<Coupon>>() {
        });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(CouponStatus.PLAYED, playedCoupons.get(0).getStatus());
        assertEquals(Long.valueOf(1), playedCoupons.get(0).getUserId());

        verify(mockOutgoingRestService, times(1)).updateUserBalance(1L, -5d);

    }

    @Test
    @Sql("/insert_coupon.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "/clean_all.sql")
    public void buyCoupon_Should_Return400_When_Balance_NotEnough() {
        OutgoingRestService mockOutgoingRestService = (OutgoingRestService) applicationContext.getBean("outgoingRestService");
        when(mockOutgoingRestService.findUserBalance(1L)).thenReturn(prepareUserBalanceResponse(1, 4));

        ArrayOfCoupons coupons = prepareCouponsRequestBody(1);
        ResponseEntity<Response> response = restTemplate.exchange(createURI("/users/1/buy"), HttpMethod.PUT, createHttpEntity(coupons), Response.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User balance is not suitable for this operation! userId: 1", response.getBody().getError());
    }

    @Test
    @Sql("/insert_coupon.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "/clean_all.sql")
    public void concurrent_BuySameCoupon_Should_Update_OnlyOne_UserBalance() {
        OutgoingRestService mockOutgoingRestService = (OutgoingRestService) applicationContext.getBean("outgoingRestService");
        UserBalance mockUserBalance = Mockito.mock(UserBalance.class);

        when(mockOutgoingRestService.findUserBalance(any(Long.class))).thenReturn(mockUserBalance);
        when(mockUserBalance.getBalance()).thenReturn(30.00);

        int concurrencyLevel = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(concurrencyLevel);

        ArrayOfCoupons coupons = prepareCouponsRequestBody(1);

        /**
         * 100 tane farklı user , id 'si 1 olan aynı kuponu, concurrent olarak almaya çalışıyor.
         * {@link kizilay.yusuf.couponapi.repository.CouponRepository#findById(Long)} üzerinde pesimistic lock olduğu
         * için , sadece tek bir user kuponu alıp, user bakiyesini update ediyor. Diğer kullanıclar için
         * {@link OutgoingRestService#updateUserBalance(Long, double)} çağrılmıyor.
         */

        HttpEntity<ArrayOfCoupons> httpEntity = createHttpEntity(coupons);

        AtomicInteger userId = new AtomicInteger(1);

        CopyOnWriteArrayList<ResponseEntity<Response>> responses
                = new CopyOnWriteArrayList<>();

        for (int i = 0; i < concurrencyLevel; i++) {
            executorService.submit(() -> {

                String uriPostFix = "/users/" + userId.getAndIncrement() + "/buy";
                ResponseEntity<Response> response = restTemplate.exchange(createURI(uriPostFix), HttpMethod.PUT, httpEntity, Response.class);
                responses.add(response);

            });
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
        } catch (InterruptedException e) {

        }

        List<String> errors = responses.stream().filter(responseEntity -> responseEntity.getStatusCode().equals(HttpStatus.BAD_REQUEST))
                .map(responseResponseEntity -> responseResponseEntity.getBody().getError()).collect(Collectors.toList());
        long success = responses.stream().filter(responseEntity -> responseEntity.getStatusCode().equals(HttpStatus.OK)).count();

        assertEquals(99, errors.size());
        assertEquals(1L, success);
        assertEquals("The coupon is not in required status! couponId: 1, Status: PLAYED", errors.get(0));
        verify(mockOutgoingRestService, times(1)).updateUserBalance(any(), anyDouble());
    }

    @Test
    @Sql("/insert_coupon.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "/clean_all.sql")
    public void cancelCoupon_Should_Return400_When_CancelDurationExpired() throws InterruptedException {
        //buy coupon
        OutgoingRestService mockOutgoingRestService = (OutgoingRestService) applicationContext.getBean("outgoingRestService");
        when(mockOutgoingRestService.findUserBalance(1L)).thenReturn(prepareUserBalanceResponse(1, 30));

        ArrayOfCoupons coupons = prepareCouponsRequestBody(1);
        restTemplate.exchange(createURI("/users/1/buy"), HttpMethod.PUT, createHttpEntity(coupons), Response.class);

        //Cancel
        // Test için cancel edilebilen max süre 5 sn olarak setlendi.
        Thread.sleep(10000);


        CouponResource coupon = prepareCouponRequestBody(1);
        ResponseEntity<Response> response = restTemplate.exchange(createURI("/users/1/cancel"), HttpMethod.PUT, createHttpEntity(coupon), Response.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("The coupon can not be canceled. Duration is expired! couponId: 1", response.getBody().getError());
    }

    @Test
    @Sql("/insert_coupon.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "/clean_all.sql")
    public void cancelCoupon_Should_Return400_When_CouponDoesNotBelongUser() {
        //buy coupon : user 1 coupon 1'yi satın alıyor
        OutgoingRestService mockOutgoingRestService = (OutgoingRestService) applicationContext.getBean("outgoingRestService");
        when(mockOutgoingRestService.findUserBalance(1L)).thenReturn(prepareUserBalanceResponse(1, 30));

        ArrayOfCoupons coupons = prepareCouponsRequestBody(1);
        restTemplate.exchange(createURI("/users/1/buy"), HttpMethod.PUT, createHttpEntity(coupons), Response.class);

        //cancel coupon : user 2 coupon 1 yi iptal etmek istiyor
        CouponResource coupon = prepareCouponRequestBody(1);
        ResponseEntity<Response> response = restTemplate.exchange(createURI("/users/2/cancel"), HttpMethod.PUT, createHttpEntity(coupon), Response.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("The coupon does not belong to this user! couponId: 1, userId: 2", response.getBody().getError());
    }

    @Test
    @Sql("/insert_coupon.sql")
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "/clean_all.sql")
    public void cancelCoupon_Should_Return200_When_RequestValid() {
        //buy coupon : user 1 coupon 1'yi satın alıyor
        OutgoingRestService mockOutgoingRestService = (OutgoingRestService) applicationContext.getBean("outgoingRestService");
        when(mockOutgoingRestService.findUserBalance(1L)).thenReturn(prepareUserBalanceResponse(1, 30));

        ArrayOfCoupons coupons = prepareCouponsRequestBody(1);
        restTemplate.exchange(createURI("/users/1/buy"), HttpMethod.PUT, createHttpEntity(coupons), Response.class);

        //cancel coupon : user 1 coupon 1 yi iptal etmek istiyor
        CouponResource coupon = prepareCouponRequestBody(1);
        ResponseEntity<Response> response = restTemplate.exchange(createURI("/users/1/cancel"), HttpMethod.PUT, createHttpEntity(coupon), Response.class);


        Coupon canceledCoupon = deserialize(response.getBody().getSuccess(), Coupon.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(CouponStatus.CANCELED, canceledCoupon.getStatus());
        assertEquals(Long.valueOf(1), canceledCoupon.getUserId());

        verify(mockOutgoingRestService, times(1)).updateUserBalance(1L, 5d);


    }

    private <T> HttpEntity<T> createHttpEntity(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        return new HttpEntity<>(body, headers);
    }

    private EventResource prepareEventRequestBody(int id) {
        EventResource eventResource = new EventResource();
        eventResource.setEventId(Long.valueOf(id));

        return eventResource;
    }

    private CouponResource prepareCouponRequestBody(int id) {
        CouponResource resource = new CouponResource();
        resource.setCouponId(Long.valueOf(id));

        return resource;
    }


    private ArrayOfEvents prepareEventsRequestBody(int... args) {
        ArrayOfEvents arrayOfEvents = new ArrayOfEvents();
        List<EventResource> eventResources = new ArrayList<>();

        int size = args.length;

        for (int i = 0; i < size; i++) {
            eventResources.add(prepareEventRequestBody(args[i]));
        }

        arrayOfEvents.setEvents(eventResources);

        return arrayOfEvents;
    }

    private ArrayOfCoupons prepareCouponsRequestBody(int... args) {
        ArrayOfCoupons arrayOfCoupons = new ArrayOfCoupons();
        Set<CouponResource> couponResources = new HashSet<>();

        int size = args.length;

        for (int i = 0; i < size; i++) {
            couponResources.add(prepareCouponRequestBody(args[i]));
        }

        arrayOfCoupons.setCoupons(couponResources);

        return arrayOfCoupons;
    }

    private UserBalance prepareUserBalanceResponse(int id, double balance) {
        UserBalance userBalance = new UserBalance();
        userBalance.setUserId(Long.valueOf(id));
        userBalance.setBalance(balance);

        return userBalance;
    }

    private <T> T deserialize(JsonNode node, Class<T> type) {
        try {
            return mapper.treeToValue(node, type);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
    }

    private <T> T deserialize(JsonNode node, TypeReference<T> typeReference) {
        ObjectReader reader = mapper.readerFor(typeReference);

        try {
            return reader.readValue(node);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private String createURI(String str) {
        StringBuilder builder = new StringBuilder(HOST);
        return builder.append(port).append(API_URL).append(str).toString();
    }
}
