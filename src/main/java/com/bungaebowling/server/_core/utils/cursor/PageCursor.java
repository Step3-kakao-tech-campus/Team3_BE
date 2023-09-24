package com.bungaebowling.server._core.utils.cursor;

public record PageCursor<T> (
        CursorRequest nextCursorRequest,
        T body
) {
}