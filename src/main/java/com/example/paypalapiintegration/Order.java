package com.example.paypalapiintegration;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "status")
    private String status;
}
