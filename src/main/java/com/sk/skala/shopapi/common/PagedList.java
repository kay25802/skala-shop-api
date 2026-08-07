package com.sk.skala.shopapi.common;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedList<T> {
    private List<T> items;
    private int page;
    private int count;
    private long totalElements;
    private int totalPages;
}
