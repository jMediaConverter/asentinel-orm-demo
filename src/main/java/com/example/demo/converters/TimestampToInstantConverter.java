package com.example.demo.converters;

import java.time.Instant;
import java.time.OffsetDateTime;

import org.springframework.core.convert.converter.Converter;

public class TimestampToInstantConverter implements Converter<OffsetDateTime, Instant> {

	@Override
	public Instant convert(OffsetDateTime source) {
		return source.toInstant();
	}
}
