package project.hamrosewa.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import project.hamrosewa.dto.UserDTO;

@Configuration
public class ApplicationConfig {

    /**
     * Create an ObjectMapper bean for JSON serialization and deserialization
     * Used by the OTP verification service to store and retrieve registration data
     * Also properly handles Java 8 date/time types like LocalDateTime
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        // Register mix-in to handle non-serializable MultipartFile
        objectMapper.addMixIn(UserDTO.class, UserDTOMixIn.class);
        
        return objectMapper;
    }
}
