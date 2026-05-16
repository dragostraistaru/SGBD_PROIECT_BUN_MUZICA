package org.example.sgbd_proiect_bun_muzica.util.paging;

import java.util.List;

/**
 * Container pentru o pagină de rezultate
 */
public class Page<E> {
    private final List<E> content;
    private final int pageNumber;
    private final int pageSize;
    private final long totalElements;

    public Page(List<E> content, int pageNumber, int pageSize, long totalElements) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
    }

    public List<E> getContent() {
        return content;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public long getTotalPages() {
        return (totalElements + pageSize - 1) / pageSize;
    }

    public boolean isFirst() {
        return pageNumber == 0;
    }

    public boolean isLast() {
        return pageNumber >= getTotalPages() - 1;
    }

    @Override
    public String toString() {
        return "Page{" +
                "content=" + content.size() +
                ", pageNumber=" + pageNumber +
                ", pageSize=" + pageSize +
                ", totalElements=" + totalElements +
                ", totalPages=" + getTotalPages() +
                '}';
    }
}

