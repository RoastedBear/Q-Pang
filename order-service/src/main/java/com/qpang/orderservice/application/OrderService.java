package com.qpang.orderservice.application;

import com.qpang.orderservice.infrastructure.kafka.OrderEventProducer;
import com.qpang.orderservice.infrastructure.kafka.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import com.qpang.common.exception.CustomException;
import com.qpang.orderservice.domain.OrderStatus;
import com.qpang.orderservice.domain.entity.Order;
import com.qpang.orderservice.domain.entity.OrderItem;
import com.qpang.orderservice.domain.repository.OrderRepository;
import com.qpang.orderservice.exception.OrderErrorCode;
import com.qpang.orderservice.infrastructure.client.DeliveryClient;
import com.qpang.orderservice.infrastructure.client.ProductStockClient;
import com.qpang.orderservice.infrastructure.client.ProductStockFeignRequest;
import com.qpang.orderservice.presentation.dto.request.CreateOrderRequest;
import com.qpang.orderservice.presentation.dto.response.OrderResponse;
import com.qpang.orderservice.presentation.dto.response.OrderSummaryResponse;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductStockClient productStockClient;
    private final OrderEventProducer orderEventProducer;
    private static final List<Integer> ALLOWED_PAGE_SIZES = List.of(10, 30, 50);

    public Order createOrder(CreateOrderCommand command){
        for(var item : command.items()){
            productStockClient.decreaseStock(
                    item.productId(),
                    new ProductStockFeignRequest(item.quantity()));
        }try{
            return orderRepository.save(command.toOrder());
        }catch(RuntimeException e){
            for(var item : command.items()){
                try{
                    productStockClient.increaseStock(
                            item.productId(),
                            new ProductStockFeignRequest(item.quantity()));
                } catch(Exception i){}
            }
            throw e;
        }
    }

    public OrderResponse createOrderFromRequest(CreateOrderRequest req, UUID userId, String userRole) {
        List<CreateOrderItemCommand> lines =
                req.items().stream()
                        .map(i -> new CreateOrderItemCommand(i.productId(), i.quantity()))
                        .toList();

        CreateOrderCommand command = new CreateOrderCommand(
                req.supplyCompanyId(),
                req.requestCompanyId(),
                userId,
                req.price(),
                req.desiredArrival(),
                req.requestMemo(),
                userId,
                lines
        );

        Order order = createOrder(command);

        List<OrderCreatedEvent.OrderItemEvent> itemEvents = lines.stream()
                .map(i -> new OrderCreatedEvent.OrderItemEvent(i.productId(), i.quantity()))
                .toList();

        orderEventProducer.sendOrderCreated(new OrderCreatedEvent(
                order.getId(),
                order.getSupplyCompanyId(),
                order.getRequestCompanyId(),
                userId,
                itemEvents
        ));

        orderRepository.save(order);
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderResponse(UUID orderId){
        return OrderResponse.from(getOrder(orderId));
    }

    private Pageable pageable(int page, int size, String sortBy, String sortDirection){
        int pageSize = ALLOWED_PAGE_SIZES.contains(size) ? size : 10;
        String property = "updatedAt".equalsIgnoreCase(sortBy) ? "updatedAt" : "createdAt";
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return PageRequest.of(page, pageSize, Sort.by(direction, property));
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getOrderSummaryList(int page, int size, String sortBy, String sortDirection){
        Pageable pageable = pageable(page, size, sortBy, sortDirection);
        return getOrderList(pageable).map(o->new OrderSummaryResponse(
                o.getId(),
                o.getStatus(),
                o.getPrice(),
                o.getCreatedAt()));
    }

    @Transactional(readOnly = true)
    public Order getOrder(UUID orderId){
        Order order = getActiveOrder(orderId);
        order.getItems().size();
        return order;
    }

    @Transactional(readOnly = true)
    public Page<Order> getOrderList(Pageable pageable){
        return orderRepository.findAllByDeletedAtIsNull(pageable);
    }

    public void changeOrderStatus(UUID orderId, OrderStatus newStatus){
        Order order = getActiveOrder(orderId);
        order.changeStatus(newStatus);
    }

    public void cancelOrder(UUID orderId){
        Order order = getOrder(orderId);
        if(order.getStatus() == OrderStatus.CANCELLED){
            throw new CustomException(OrderErrorCode.ORDER_ALREADY_CANCELED);
        }
        for(OrderItem line : order.getItems()){
            productStockClient.increaseStock(
                    line.getProductId(),
                    new ProductStockFeignRequest(line.getQuantity()));
        }
        order.changeStatus(OrderStatus.CANCELLED);
    }

    public void deleteOrder(UUID orderId, UUID deletedBy){
        Order order = getActiveOrder(orderId);
        order.softDelete(deletedBy);
    }

    private Order getActiveOrder(UUID orderId){
        return orderRepository.findById(orderId).map(order->{
            if(order.getDeletedAt() != null){
                throw new CustomException(OrderErrorCode.ORDER_ALREADY_DELETED);
            }
            return order;
        }).orElseThrow(()
                -> new CustomException(OrderErrorCode.ORDER_NOT_FOUND));
    }

    @Transactional
    public void cancelOrderBySystem(UUID orderId, String reason) {
        Order order = orderRepository.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new CustomException(OrderErrorCode.ORDER_NOT_FOUND));
        order.changeStatus(OrderStatus.CANCELLED);
        log.warn("[Saga 보상 트랜잭션] 주문 취소 처리 완료 - orderId: {}, reason: {}", orderId, reason);
    }
}
