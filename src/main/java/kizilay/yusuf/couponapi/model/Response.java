package kizilay.yusuf.couponapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Response<T> extends BaseResource {

    private static final ObjectMapper mapper = new ObjectMapper();

    private JsonNode success;

    @JsonIgnore
    private T value;

    private String error;

    public Response(T value) {
        this.success = mapper.convertValue(value, JsonNode.class);
    }

    public Response(String error) {
        this.error = error;
    }
}
