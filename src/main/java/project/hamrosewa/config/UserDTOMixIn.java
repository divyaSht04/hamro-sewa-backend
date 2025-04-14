package project.hamrosewa.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.web.multipart.MultipartFile;

/**
 * Jackson mix-in to handle non-serializable MultipartFile in UserDTO
 */
public abstract class UserDTOMixIn {
    @JsonIgnore
    abstract MultipartFile getImage();
}
