package com.codeit.team4.deokhugam.book.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OcrResponse(
        @JsonProperty("ParsedResults") List<OcrParsedResult> parsedResults,
        @JsonProperty("IsErroredOnProcessing") boolean isErroredOnProcessing
) {

    public record OcrParsedResult(
            @JsonProperty("ParsedText") String parsedText
    ) {

    }
}