package de.ait.javalessonspro.dto;

import java.util.List;

public class PageResponse <T>{
    private final List<T> content;
    private final int page;
    private final int size;
    private final int totalPages;
    private final long totalElements;
    private final boolean last;
    private final boolean first;

    public PageResponse(List<T> content, int page, int size, int totalPages, long totalElements, boolean last, boolean first) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.last = last;
        this.first = first;
    }

    public List<T> getContent() {
        return content;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public boolean isLast() {
        return last;
    }

    public boolean isFirst() {
        return first;
    }
}
