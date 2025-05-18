package entrenasync.dev.entrenasyncapigateway.Storage.Exceptions;

public class CloudinaryException extends RuntimeException {
    public CloudinaryException(String message) {
        super(message);
    }

    public static class UploadingException extends CloudinaryException {
        public UploadingException() {super("Error while uploading an image");}
    }

    public static class PublicIdNotFoundException extends CloudinaryException {
        public PublicIdNotFoundException() {super("Image public ID is required");}
    }

    public static class RetrievingImageException extends CloudinaryException {
        public RetrievingImageException() {super("Error retrieving image from Cloudinary");}
    }

    public static class DeletingException extends CloudinaryException {
        public DeletingException() {super("Error deleting the image from Cloudinary");}
    }
}
