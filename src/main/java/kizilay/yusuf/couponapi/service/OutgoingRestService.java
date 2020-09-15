package kizilay.yusuf.couponapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kizilay.yusuf.couponapi.execption.UserNotFoundException;
import kizilay.yusuf.couponapi.model.ChangeAmountResource;
import kizilay.yusuf.couponapi.model.Response;
import kizilay.yusuf.couponapi.model.UserBalance;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class OutgoingRestService {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final ObjectMapper mapper = new ObjectMapper();


    //Servis discovery ve servis registration için eureka server kullanabilirdim.
    //URL'i hardcoded yazmak , iyi bir fikir değil ama case studyde özellikle istenmediğinden ve zaman kısıtlarından dolayı
    //böyle yaptım

    private static final String BALANCE_API_URL = "http://localhost:8081/bilyoner/userBalances";
    private static final String SLASH = "/";

    public UserBalance findUserBalance(Long userId) {
        StringBuilder builder = new StringBuilder(BALANCE_API_URL);
        String uri = builder.append(SLASH).append(userId).toString();

        ResponseEntity<Response> response = null;

        try {
            response = restTemplate.getForEntity(uri, Response.class);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                throw new UserNotFoundException(String.format("User is not found! userId: %d", userId));
            }

            throw ex;
        }

        return deserialize(response.getBody().getSuccess(), UserBalance.class);
    }

    public void updateUserBalance(final Long userId, final double changedAmount) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        HttpEntity<ChangeAmountResource> request = new HttpEntity<>(new ChangeAmountResource(changedAmount), headers);

        StringBuilder builder = new StringBuilder(BALANCE_API_URL);

        String uri = builder.append(SLASH).append(userId).toString();

        restTemplate.exchange(uri, HttpMethod.PUT, request, UserBalance.class);
    }

    private <T> T deserialize(JsonNode node, Class<T> type) {
        try {
            return mapper.treeToValue(node, type);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
    }
}
