package com.springboot.orderservice.service;

import com.springboot.orderservice.dto.InventoryResponse;
import com.springboot.orderservice.dto.OrderLineItemsDto;
import com.springboot.orderservice.dto.OrderRequest;
import com.springboot.orderservice.model.Order;
import com.springboot.orderservice.model.OrderLineItems;
import com.springboot.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    public void PlaceOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
         List<OrderLineItems> orderLineItems= orderRequest.getOrderLineItemsDtoList()
                 .stream()
                .map(orderLineItemsDto -> maptoDto(orderLineItemsDto)).collect(Collectors.toList());

        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = order.getOrderLineItemsList().stream().map(orderLineItem ->orderLineItem.getSkuCode()).toList();
        //call Inventory service, and place order if product is in stock

        InventoryResponse[] inventoryResponsesArray = webClientBuilder.build().get()
                .uri("http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                 .retrieve()//to retrieve the response and need to specify the type of response that is done using mono
                 .bodyToMono(InventoryResponse[].class) //to be able to read the response by webclient we need to mention bodytomono method -- need to mention type of response
                  .block(); // will make a sync request with the help of block

        boolean allProductsInstock = Arrays.stream(inventoryResponsesArray).allMatch(inventoryResponse -> inventoryResponse.isInStock());
        // verify does inventory response -- we will check wether all products are in stock or not of the particular order if one of them is not it will return false;

        if(allProductsInstock){
            orderRepository.save(order);
        }else{
            throw new IllegalArgumentException("Product is not in stock,please try again later");
        }


        orderRepository.save(order);
    }

    private OrderLineItems maptoDto(OrderLineItemsDto orderLineItemsDto){
        OrderLineItems orderLineItems=new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setId(orderLineItemsDto.getId());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        return orderLineItems;
    }
}
