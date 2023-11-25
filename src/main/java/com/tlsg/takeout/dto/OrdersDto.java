package com.tlsg.takeout.dto;

import com.tlsg.takeout.entity.OrderDetail;
import com.tlsg.takeout.entity.Orders;
import lombok.Data;

import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;

}