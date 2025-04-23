package com.huynhduc.application.service;
import com.huynhduc.application.entity.Order;
import com.huynhduc.application.model.dto.OrderDetailDTO;
import com.huynhduc.application.model.dto.OrderInfoDTO;
import com.huynhduc.application.model.request.CreateOrderRequest;
import com.huynhduc.application.model.request.UpdateDetailOrder;
import com.huynhduc.application.model.request.UpdateStatusOrderRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface OrderService {
    Page<Order> adminGetListOrders(String id, String name, String phone, String status, String product, int page);

    Order createOrder(CreateOrderRequest createOrderRequest , Long id);

    Order createOrder(CreateOrderRequest createOrderRequest);

    void updateDetailOrder(UpdateDetailOrder updateDetailOrder, long id, long userId);

    Order findOrderById(long id);

    void updateStatusOrder(UpdateStatusOrderRequest updateStatusOrderRequest, long orderId, long userId);

    List<OrderInfoDTO> getListOrderOfPersonByStatus(int status, long userId);

    OrderDetailDTO userGetDetailById(long id, long userId);

    void userCancelOrder(long id, long userId);

    //Đếm số lượng đơn hàng
    long getCountOrder();

    Order findById(Long orderId);

    public boolean updateOrderStatus(String orderId, String status);
}
