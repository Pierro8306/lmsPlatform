package pierro.lmsPlatform.dto.request.Learning;

/** path = value stored in the DB, url = direct public URL for <video>/<iframe>/<img>. */
public record UploadedFileDto(String path, String url, String contentType, long size) {
}
