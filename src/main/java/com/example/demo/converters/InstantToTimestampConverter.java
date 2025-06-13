package com.example.demo.converters;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.Set;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.jdbc.core.SqlParameterValue;

import com.asentinel.common.orm.mappers.SqlParameterTypeDescriptor;

public class InstantToTimestampConverter implements ConditionalGenericConverter {

	@Override
	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		Instant instant = (Instant) source;
		Timestamp t = Timestamp.from(instant);
		return new SqlParameterValue(Types.TIMESTAMP, t);
	}

	@Override
	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (sourceType.getType() != Instant.class) {
			return false;
		}
		if (!(targetType instanceof SqlParameterTypeDescriptor)) {
			return false;
		}			
		SqlParameterTypeDescriptor typeDescriptor = (SqlParameterTypeDescriptor) targetType;
		return "timestamp".equals(typeDescriptor.getTypeName());
	}
	
	@Override
	public Set<ConvertiblePair> getConvertibleTypes() {
		return null;
	}

}