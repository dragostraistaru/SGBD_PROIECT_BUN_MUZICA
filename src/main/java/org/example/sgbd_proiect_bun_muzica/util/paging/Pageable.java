package org.example.sgbd_proiect_bun_muzica.util.paging;

/**
 * Configurare pentru paginare
 */
public class Pageable {
    private final int pageNumber; // 0-based
    private final int pageSize;

    public Pageable(int pageNumber, int pageSize) {
        this.pageNumber = Math.max(0, pageNumber);
        this.pageSize = Math.max(1, pageSize);
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getOffset() {
        return pageNumber * pageSize;
    }

    @Override
    public String toString() {
        return "Pageable{" +
                "pageNumber=" + pageNumber +
                ", pageSize=" + pageSize +
                ", offset=" + getOffset() +
                '}';
    }
}

