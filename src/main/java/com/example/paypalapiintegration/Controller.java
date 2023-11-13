package com.example.paypalapiintegration;

import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class Controller {
    private final OkHttpClient client;
    private final Service service;

    @Value("${app.username}")
    private String username;
    @Value("${app.password}")
    private String password;


    @GetMapping("/capture")
    public ResponseEntity<Void> captureOrder(@RequestParam(name = "token") String token){
        MediaType mediaType = MediaType.parse("application/json");

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://api.sandbox.paypal.com/v2/checkout/orders/"+token+"/capture")
                .header("Content-Type", "application/json")
                .header("Authorization","Bearer A21AAIJ7FeYvOKHa_uNpfIoZlXvx7r_EpvV4wS25F9xvewBZYTOCcjBJj6Q4GNO-b3M1B9jxviiriVV4n0O-COkS1jRsp7PIA")
                .post(okhttp3.RequestBody.create(mediaType, new String()))
                .build();
        try (Response response = client.newCall(request).execute()) {
            Order order = service.findById(token);
            order.setStatus("COMPLETED");
            service.save(order);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok().build();
    }
    @GetMapping("/cancel")
    public void cancelOrder(@RequestParam(name = "token") String token){
        Order order = service.findById(token);
        order.setStatus("CANCELLED");
        service.save(order);
    }

    @PostMapping("/create")
    public ResponseEntity<String> createOrder(@RequestBody CreateOrderRequest createOrderRequest){
        String credentials = Credentials.basic(username, password);

        okhttp3.RequestBody requestBodyForAccessToken = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .build();

        Request requestForAccessToken = new Request.Builder()
                .url("https://api-m.sandbox.paypal.com/v1/oauth2/token")
                .header("Authorization", credentials) // Додавання заголовку автентифікації
                .post(requestBodyForAccessToken) // Якщо використовується метод POST
                .build();

        String accessToken = null;
        try(Response response = client.newCall(requestForAccessToken).execute()) {
            String responseBody =response.body().string();
            JSONObject jsonObject = new JSONObject(responseBody);
            accessToken = (String) jsonObject.get("access_token");
        } catch (Exception e) {
            e.printStackTrace();
        }

        MediaType mediaType = MediaType.parse("application/json");

        JSONObject requestBody = service.getJsonForCreatingOrder(createOrderRequest);

        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, requestBody.toString());
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://api-m.sandbox.paypal.com/v2/checkout/orders")
                .header("Content-Type", "application/json")
                .header("Authorization","Bearer "+accessToken)
                .post(body)
                .build();
        String id;
        try (Response response = client.newCall(request).execute()) {
            String responseBody =response.body().string();
            JSONObject jsonObject = new JSONObject(responseBody);
            id = (String) jsonObject.get("id");

            Order order = new Order(id,"CREATED");
            service.save(order);

            JSONArray links = jsonObject.getJSONArray("links");
            return ResponseEntity.ok(service.getApproveLink(links));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.internalServerError().build();
    }
}
