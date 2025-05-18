package entrenasync.dev.entrenasyncapigateway.Storage.Controller;

import entrenasync.dev.entrenasyncapigateway.Storage.Services.CloudinaryService;
import entrenasync.dev.entrenasyncapigateway.Storage.dto.UploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/storage/images")
public class CloudinaryController {

    private final CloudinaryService cloudinaryService;

    @Autowired
    public CloudinaryController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<UploadResponse>> uploadImage(@RequestPart("file") FilePart filePart) {
        log.debug("Uploading image...");
        if (filePart == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty file");
        }
        return cloudinaryService.uploadImage(filePart)
                .map(response -> {
                    log.debug("Image uploaded successfully");
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(e -> {
                    log.error("Error uploading image", e);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There was an error uploading the image: " , e);
                });
    }



    @GetMapping("/{publicId}")
    public ResponseEntity<?> getImage(@PathVariable String publicId) {
        try {
            return ResponseEntity.ok(cloudinaryService.getImage(publicId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving image: " + e.getMessage());
        }
    }

    @DeleteMapping("/{publicId}")
    public ResponseEntity<?> deleteImage(@PathVariable String publicId) {
        try {
            return ResponseEntity.ok(cloudinaryService.deleteImage(publicId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting image: " + e.getMessage());
        }
    }
}
