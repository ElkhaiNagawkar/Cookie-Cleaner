import burp.api.montoya.MontoyaApi;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CookieScanEngine {
    private final MontoyaApi api;
    public CookieScanEngine(MontoyaApi api){
        this.api = api;
    }

    public List<String> getCookiesFromHistory(){
        return api.proxy().history().stream()
                .filter(msg -> api.scope().isInScope(msg.request().url()))
                .filter(msg -> msg.request().headerValue("Cookie") != null)
                .flatMap(msg -> Arrays.stream(msg.request().headerValue("Cookie").split("; "))).map(cookie -> cookie.split("=")[0])
                .collect(Collectors.toList());
    }
}