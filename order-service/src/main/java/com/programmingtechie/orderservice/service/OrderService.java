package com.programmingtechie.orderservice.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.programmingtechie.orderservice.dto.OrderLineItemsDto;
import com.programmingtechie.orderservice.dto.OrderRequest;
import com.programmingtechie.orderservice.model.Order;
import com.programmingtechie.orderservice.model.OrderLineItems;
import com.programmingtechie.orderservice.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

/*
 * @RequiredArgsConstructor is used for creating a parameterized constructor for constructor based dependency injection.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

	private final OrderRepository orderRepository;

	public void placeOrder(OrderRequest orderRequest) {
		Order order = new Order();
		order.setOrderNumber(UUID.randomUUID().toString());

		// OrderRequest will be a List of OrderLineItemsDto. Hence using this stream to
		// convert it into a List of OrderLineItems in order to be able to save it into
		// an Order pojo.
		List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList().stream()
				.map(orderLineItemsDto -> mapToDto(orderLineItemsDto)).toList();

		order.setOrderLineItemsList(orderLineItems);
		orderRepository.save(order);
	}

	/**
	 * This method is for converting the object of OrderLineItemsDto into an object
	 * of OrderLineItems. Note: OrderLineItems and OrderLineItemsDto are basically
	 * the same thing. But in general, it is a good practice to create a replica
	 * object of the main object which is there in model package. This is for
	 * security reasons.
	 */
	private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
		OrderLineItems orderLineItems = new OrderLineItems();
		orderLineItems.setPrice(orderLineItemsDto.getPrice());
		orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
		orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());

		return orderLineItems;
	}
}
