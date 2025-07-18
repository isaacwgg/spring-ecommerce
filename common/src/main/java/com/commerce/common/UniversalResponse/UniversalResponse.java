package com.commerce.common.UniversalResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class UniversalResponse {
    private Integer status;
    private String message;
    private Object data;

}
