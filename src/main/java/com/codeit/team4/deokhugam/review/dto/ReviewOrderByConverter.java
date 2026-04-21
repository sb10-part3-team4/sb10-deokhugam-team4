package com.codeit.team4.deokhugam.review.dto;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ReviewOrderByConverter implements Converter<String, ReviewOrderBy> {

    @Override
    public ReviewOrderBy convert(String source) {
        String formatted = source.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
        return ReviewOrderBy.valueOf(formatted);
    }
}
