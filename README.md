# paypal-api-integration

## API Reference

#### Create order

```http
  POST /create
```
### Accepts

| Parameter | Type                 |
| :-------- | :------------------- |
| `request` | `CreateOrderRequest` |

### `CreateOrderRequest` {
    List<Item> items;
    String currencyCode;
}

### `Item` {
    String name;
    Double value;
    String description;
    Integer quantity;
}

### Returns
Returns a `link` to the page to which the user needs to be redirected for payment


