package com.codeit.team4.deokhugam.book.dto;

import java.util.List;

public record OcrResponse(
        List<OcrParsedResult> ParsedResults,
        boolean IsErroredOnProcessing
) {
    public record OcrParsedResult(
            String ParsedText
    ) {}
}