package os.assurance.eu.api.evidence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;

@Converter
public class CitationListConverter implements AttributeConverter<List<Citation>, String> {
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final TypeReference<List<Citation>> CITATION_LIST = new TypeReference<>() {
  };

  @Override
  public String convertToDatabaseColumn(List<Citation> attribute) {
    try {
      return MAPPER.writeValueAsString(attribute == null ? List.of() : attribute);
    } catch (Exception e) {
      throw new IllegalArgumentException("Could not serialize citations", e);
    }
  }

  @Override
  public List<Citation> convertToEntityAttribute(String dbData) {
    try {
      if (dbData == null || dbData.isBlank()) {
        return List.of();
      }
      return MAPPER.readValue(dbData, CITATION_LIST);
    } catch (Exception e) {
      throw new IllegalArgumentException("Could not deserialize citations", e);
    }
  }
}
