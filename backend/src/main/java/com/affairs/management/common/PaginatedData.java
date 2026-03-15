package com.affairs.management.common;

import lombok.Data;
import java.util.List;

/**
 * 分页数据包装
 */
@Data
public class PaginatedData<T> {
    private List<T> list;
    private long total;
    private int page;
    private int pageSize;

    public PaginatedData() {}

    public PaginatedData(List<T> list, long total, int page, int pageSize) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }

    public static <T> PaginatedData<T> of(List<T> list, long total, int page, int pageSize) {
        return new PaginatedData<>(list, total, page, pageSize);
    }
}
