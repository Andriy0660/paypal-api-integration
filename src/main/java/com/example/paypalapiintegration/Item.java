package com.example.paypalapiintegration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    String name;
    Double value;
    String description;
    Integer quantity;
}
