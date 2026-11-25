package com.harmoni.pos.order.mapper;

import com.harmoni.pos.order.model.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;
import java.util.List;

@Mapper
public interface OrderMapper {

    int deleteByPrimaryKey(Integer id);
    int insert(Order order);
    Order selectByPrimaryKey(Integer id);
    Order selectByIdWithPayment(Integer id);
    Order selectByIdempotencyKey(String idempotencyKey);
    List<Order> selectByStoreId(Integer storeId, Timestamp from, Timestamp until);

    /**
     * One page of a store's orders, newest first, using a keyset cursor.
     * <p>
     * Paging on {@code id} rather than on an offset is what keeps the list stable while it
     * is being read: a new order confirmed midway through scrolling shifts every offset by
     * one, so an offset page would repeat a row the cashier has already seen. {@code id <
     * beforeId} cannot.
     * <p>
     * The limit is applied to the orders, not to the joined rows. A product with three SKU
     * lines and four choices produces a dozen joined rows, and a plain {@code LIMIT} at the
     * end would cut an order in half and return one with lines missing.
     *
     * @param storeId  the store whose orders are wanted
     * @param from     start of the range, inclusive
     * @param until    end of the range, exclusive
     * @param beforeId return only orders older than this id; {@code null} for the first page
     * @param limit    the most orders to return
     * @return the page, in the order it is displayed
     */
    List<Order> selectByStoreIdPaged(@Param("storeId") Integer storeId,
                                     @Param("from") Timestamp from,
                                     @Param("until") Timestamp until,
                                     @Param("beforeId") Integer beforeId,
                                     @Param("limit") int limit);
    int updateByPrimaryKey(Order order);
    int voidById(Order order);
}