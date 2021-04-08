package com.smart.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smart.entities.MyPaymenrOrder;

public interface MyPaymentOrderRepo extends JpaRepository<MyPaymenrOrder, Long> {
	
	
	public MyPaymenrOrder findByOrderId(String orderId);

}
