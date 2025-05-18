package entrenasync.dev.entrenasyncapigateway.Storage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadResponse {
    String secureUrl;
    String publicId;
}
