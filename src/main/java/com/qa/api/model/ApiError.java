package com.qa.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Error envelope returned by Petstore for 4xx/5xx responses: {"code":1,"type":"error","message":"Pet not found"} */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiError(Integer code, String type, String message) {
}
