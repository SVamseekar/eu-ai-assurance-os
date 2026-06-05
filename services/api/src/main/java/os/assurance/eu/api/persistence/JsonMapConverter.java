package os.assurance.eu.api.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Map;

@Converter
public class JsonMapConverter implements AttributeConverter<Map<String, Object>, String> {
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
  };

  @Override
  public String convertToDatabaseColumn(Map<String, Object> attribute) {
    try {
      return MAPPER.writeValueAsString(attribute == null ? Map.of() : attribute);
    } catch (Exception e) {
      throw new IllegalArgumentException("Could not serialize JSON map", e);
    }
  }

  @Override
  public Map<String, Object> convertToEntityAttribute(String dbData) {
    try {
      if (dbData == null || dbData.isBlank()) {
        return Map.of();
      }
      return MAPPER.readValue(dbData, MAP_TYPE);
    } catch (Exception e) {
      throw new IllegalArgumentException("Could not deserialize JSON map", e);
    }
  }
}
