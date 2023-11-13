package com.example.paypalapiintegration;

import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;

import java.text.DecimalFormat;
import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class Service {
    private final Repository repository;
    @Value("${return-url}")
    private String returnUrl;
    @Value("${cancel-url}")
    private String cancelUrl;
    public Order findById(String id){ return repository.findById(id).get(); }
    public void save(Order order){ repository.save(order); }
    public JSONObject getJsonForCreatingOrder(CreateOrderRequest createOrderRequest){
        List<Item> items = createOrderRequest.getItems();
        String currencyCode = createOrderRequest.getCurrencyCode();

        JSONObject requestBody = new JSONObject();
        requestBody.put("intent", "CAPTURE");

        JSONObject appContext = new JSONObject();
        appContext.put("return_url",returnUrl);
        appContext.put("cancel_url",cancelUrl);
        requestBody.put("application_context", appContext);

        JSONArray purchaseUnits = new JSONArray();
        JSONArray itemsArrayJson = new JSONArray();
        Double amount = items.stream().mapToDouble(e -> e.getValue()*e.getQuantity()).sum();
        DecimalFormat df = new DecimalFormat("0.00");
        items.forEach(e -> {
            JSONObject itemJson = new JSONObject();
            itemJson.put("name",e.getName());
            itemJson.put("description",e.getDescription());
            itemJson.put("quantity",e.getQuantity().toString());

            JSONObject unitAmount = new JSONObject();
            unitAmount.put("currency_code",currencyCode);
            unitAmount.put("value", df.format(e.getValue()).replace(',','.'));

            itemJson.put("unit_amount",unitAmount);

            itemsArrayJson.put(itemJson);
        });

        JSONObject amountJson = new JSONObject();
        amountJson.put("currency_code", currencyCode);
        amountJson.put("value", df.format(amount).replace(',','.'));
        amountJson.put("currency_code", currencyCode);
        JSONObject breakdownJson = new JSONObject();

        JSONObject itemTotalJson = new JSONObject();
        itemTotalJson.put("currency_code", currencyCode);
        itemTotalJson.put("value", df.format(amount).replace(',','.'));

        breakdownJson.put("item_total", itemTotalJson);
        amountJson.put("breakdown", breakdownJson);

        JSONObject purchaseUnit = new JSONObject();
        purchaseUnit.put("items", itemsArrayJson);
        purchaseUnit.put("amount", amountJson);
        purchaseUnits.put(purchaseUnit);

        requestBody.put("purchase_units", purchaseUnits);
        return requestBody;
    }
    public String getApproveLink(JSONArray links){
        StringBuilder approveLink = new StringBuilder();
        links.forEach(e -> {
            String href = (String) ((JSONObject)e).get("href");
            if(((JSONObject)e).get("rel").equals("approve"))
                approveLink.append(href);
        });
        return approveLink.toString();
    }
}
