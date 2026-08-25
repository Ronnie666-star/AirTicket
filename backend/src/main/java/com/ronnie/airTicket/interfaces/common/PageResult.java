package com.ronnie.airTicket.interfaces.common;

import java.util.List;
import java.util.function.Function;

/**
 * 分页结果封装：{ total, page, size, data }。
 * total 是满足筛选条件的总条数；page 从 1 开始；data 是当前页数据。
 */
public record PageResult<T>(long total, int page, int size, List<T> data) {

    public static <T> PageResult<T> of(long total, int page, int size, List<T> data) {
        return new PageResult<>(total, page, size, data);
    }

    /** 把当前页数据整体翻译成另一种类型（如 Result -> Response），分页元信息不变。 */
    public <R> PageResult<R> map(Function<T, R> mapper) {
        return new PageResult<>(total, page, size, data.stream().map(mapper).toList());
    }

    /** 归一化页码：null 或 <1 都当第 1 页。 */
    public static int normalizePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    /** 归一化页大小：null 默认 10，夹在 [1, 100] 之间，防止一次性查太多。 */
    public static int normalizeSize(Integer size) {
        return size == null ? 10 : Math.max(1, Math.min(size, 100));
    }
}
