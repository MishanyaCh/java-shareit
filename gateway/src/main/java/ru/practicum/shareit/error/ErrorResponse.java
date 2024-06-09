package ru.practicum.shareit.error;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {
    private String error;

    public ErrorResponse(String errorArg) {
        error = errorArg;
    }

    @Override
    public String toString() {
        return  "ErrorResponse{" + "error='" + error + '\'' + "}";
    }
}
