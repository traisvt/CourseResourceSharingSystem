package com.kit.sharingsystem.model;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int number;
    private int size;

    public PageResult(List<T> content, long totalElements, int number, int size) {
        this.content = content;
        this.totalElements = totalElements;
        this.number = number;
        this.size = size;
        this.totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
    }
}
