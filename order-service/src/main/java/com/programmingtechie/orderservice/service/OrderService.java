package com.programmingtechie.orderservice.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.programmingtechie.orderservice.dto.InventoryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.programmingtechie.orderservice.dto.OrderLineItemsDto;
import com.programmingtechie.orderservice.dto.OrderRequest;
import com.programmingtechie.orderservice.model.Order;
import com.programmingtechie.orderservice.model.OrderLineItems;
import com.programmingtechie.orderservice.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;

/*
 * @RequiredArgsConstructor is used for creating a parameterized constructor for constructor based dependency injection.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;

    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        // OrderRequest will be a List of OrderLineItemsDto. Hence, using this stream to
        // convert it into a List of OrderLineItems in order to be able to save it into
        // an Order pojo.
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList().stream()
                .map(orderLineItemsDto -> mapToDto(orderLineItemsDto))
                .toList();

        order.setOrderLineItemsList(orderLineItems);

        // Getting the List of skuCodes for the particular order.
        // Order will contain OrderLineItemsList.
        // Hence, using streams to get the individual skuCodes which is String
        List<String> skuCodesList = order.getOrderLineItemsList().stream()
                .map(orderLineItem -> orderLineItem.getSkuCode())
                .toList();

        // Call Inventory service, and place order if product is in stock
        // The method in inventory-service controller class returns a boolean response.
        // Hence, using bodyToMono(Boolean.class) to mention that the response will be boolean type.
        // block is used to make synchronous request to the uri mentioned. Or else by default it is asynchronous.
//        Boolean result = webClient.get().uri("http://localhost:8082/api/inventory")
//                .retrieve()
//                .bodyToMono(Boolean.class)
//                .block();

        // As the Inventory Service has been enhanced to give the List of InventoryResponse instead of just the boolean response,
        // the above implementation for webclient call needs to be modified.
        // Modification 1: the InventoryController api endpoint will now require a RequestParam which is a List of skuCodes. Hence, we are passing that using uriBuilder.
        // Modification 2: As the response from inventory controller will now be a List of InventoryResponse object, we need to create that Pojo here as well.
        // Modification 3: bodyToMono() is changed to accommodate the InventoryResponse object.
        InventoryResponse[] inventoryResponseArray = webClient.get()
                .uri("http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodesList).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        // Here we are checking if all the products from the InventoryResponse List or array, are present or not.
        // Even if One product stock is zero, the allProductsInStock will be false. This is because we have used allMatch().
        boolean allProductsInStock = false;
        if (inventoryResponseArray != null) {
            allProductsInStock = Arrays.stream(inventoryResponseArray).allMatch(inventoryResponse -> inventoryResponse.isInStock());
        }

        if (allProductsInStock)
            orderRepository.save(order);
        else
            throw new IllegalArgumentException("ERROR: Product is not in stock, please try again later");
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
