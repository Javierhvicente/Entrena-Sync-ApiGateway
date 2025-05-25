package entrenasync.dev.entrenasyncapigateway.Utils;


import java.util.List;

public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        int totalElements,
        int totalPages
) {}
