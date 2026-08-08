package com.harmoni.pos.order.mapper;

import com.harmoni.pos.order.model.DailyReport;
import com.harmoni.pos.order.model.OrderVolumeReport;
import com.harmoni.pos.order.model.PaymentSummary;
import com.harmoni.pos.order.model.ProductSalesReport;
import com.harmoni.pos.order.model.SettlementReport;
import com.harmoni.pos.order.model.TopProductReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ReportMapper {

    SettlementReport getSettlementReport(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    List<PaymentSummary> getPaymentBreakdown(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);

    List<TopProductReport> getTopProducts(@Param("storeId") Integer storeId,
                                          @Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end,
                                          @Param("limit") int limit);

    List<DailyReport> getDailyReport(@Param("storeId") Integer storeId,
                                     @Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

    List<ProductSalesReport> getSalesReport(@Param("storeId") Integer storeId,
                                            @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    OrderVolumeReport getOrderVolumeReport(@Param("storeId") Integer storeId,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

}
