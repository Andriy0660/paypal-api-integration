package com.example.paypalapiintegration;

import org.springframework.data.jpa.repository.JpaRepository;

public interface Repository extends JpaRepository<Order, String> {
}
