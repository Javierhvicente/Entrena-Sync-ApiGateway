package entrenasync.dev.entrenasyncapigateway.Storage.Services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;
import entrenasync.dev.entrenasyncapigateway.Storage.Exceptions.CloudinaryException;
import entrenasync.dev.entrenasyncapigateway.Storage.dto.UploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.CloseNowException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
public class CloudinaryServiceImpl implements CloudinaryService {
    private final Cloudinary cloudinary;
    @Autowired
    public CloudinaryServiceImpl(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret)
    {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    public Mono<UploadResponse> uploadImage(FilePart filePart) {
        Flux<DataBuffer> content = filePart.content();

        return DataBufferUtils.join(content)
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    try {
                        Map result = cloudinary.uploader().upload(bytes, ObjectUtils.emptyMap());
                        String url = result.get("secure_url").toString();
                        String publicId = result.get("public_id").toString();
                        return Mono.just(new UploadResponse(url, publicId));
                    } catch (IOException e) {
                        log.error("Error uploading de image", e);
                        return Mono.error(new CloudinaryException.UploadingException());
                    }
                });
    }


    public Map<String, Object> getImage(String publicId)  {
        if (StringUtils.isEmpty(publicId)) throw new CloudinaryException.PublicIdNotFoundException();
        try{
            return cloudinary.api().resource(publicId, ObjectUtils.emptyMap());
        }catch (Exception e){
            throw new CloudinaryException.RetrievingImageException();
        }

    }

    public Map<String, Object> deleteImage(String publicId) {
        try {
            return cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }catch (Exception e){
            throw new CloudinaryException.DeletingException();
        }
    }
}
