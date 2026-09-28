package vn.audiostore.ai;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class GeminiConfig {
    @Bean RestClient geminiRestClient(@Value("${app.gemini.timeout-ms:12000}") long timeoutMs){
        HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();
        JdkClientHttpRequestFactory factory=new JdkClientHttpRequestFactory(client);
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        return RestClient.builder().baseUrl("https://generativelanguage.googleapis.com").requestFactory(factory).build();
    }
}
