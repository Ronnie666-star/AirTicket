package com.ronnie.airTicket.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 请求 DTO：描述"HTTP 层怎么接收参数"，带参数校验。它不属于 domain，是接口层的模型。 */
@Data
public class BookTicketRequest {

    @NotNull(message = "航班 id 不能为空")
    private Long flightId;

    @NotBlank(message = "乘机人姓名不能为空")
    @Size(max = 20, message = "姓名最长 20 字")
    private String passengerName;

    @NotBlank(message = "联系电话不能为空")
    @Size(max = 20, message = "电话最长 20 位")
    private String passengerPhone;
}
