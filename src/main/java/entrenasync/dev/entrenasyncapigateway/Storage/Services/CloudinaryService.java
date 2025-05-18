package entrenasync.dev.entrenasyncapigateway.Storage.Services;

import entrenasync.dev.entrenasyncapigateway.Storage.dto.UploadResponse;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface CloudinaryService {
    Mono<UploadResponse> uploadImage(FilePart file);
    Map<String, Object> getImage(String publicId);
    Map<String, Object> deleteImage(String publicId);
}
