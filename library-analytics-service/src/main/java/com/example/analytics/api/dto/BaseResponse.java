package com.example.analytics.api.dto;

import com.example.analytics.common.AppConstans;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@Getter
@JsonInclude(Include.NON_NULL)
@NoArgsConstructor
@Setter
@SuperBuilder
public class BaseResponse<T> {
    private String responseMessage;
    private T responseData;

    public void setResponseSucceed() {
        this.responseMessage = AppConstans.RESPONSE_MESSAGE_SUCCESS_IN;
    }
}
