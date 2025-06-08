package forge.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MimeTypeCheckService {

	private final Tika tika = new Tika();
	
	private static final Map<String, String> MIME_TYPE_TO_EXTENSION_MAP = new HashMap<>();
	
	// static field initialization
	static {
	    // Images
	    MIME_TYPE_TO_EXTENSION_MAP.put("image/jpeg", ".jpg");
	    MIME_TYPE_TO_EXTENSION_MAP.put("image/png", ".png");
	    MIME_TYPE_TO_EXTENSION_MAP.put("image/gif", ".gif");
	    MIME_TYPE_TO_EXTENSION_MAP.put("image/bmp", ".bmp");
	    MIME_TYPE_TO_EXTENSION_MAP.put("image/webp", ".webp");
	    MIME_TYPE_TO_EXTENSION_MAP.put("image/svg+xml", ".svg");
	    MIME_TYPE_TO_EXTENSION_MAP.put("image/tiff", ".tiff");
	    
	    // Audio
	    MIME_TYPE_TO_EXTENSION_MAP.put("audio/mpeg", ".mp3");
	    MIME_TYPE_TO_EXTENSION_MAP.put("audio/wav", ".wav");
	    MIME_TYPE_TO_EXTENSION_MAP.put("audio/ogg", ".ogg");
	    MIME_TYPE_TO_EXTENSION_MAP.put("audio/flac", ".flac");
	    MIME_TYPE_TO_EXTENSION_MAP.put("audio/aac", ".aac");
	    MIME_TYPE_TO_EXTENSION_MAP.put("audio/midi", ".midi");

	    // Video
	    MIME_TYPE_TO_EXTENSION_MAP.put("video/mp4", ".mp4");
	    MIME_TYPE_TO_EXTENSION_MAP.put("video/x-msvideo", ".avi");
	    MIME_TYPE_TO_EXTENSION_MAP.put("video/x-matroska", ".mkv");
	    MIME_TYPE_TO_EXTENSION_MAP.put("video/quicktime", ".mov");
	    MIME_TYPE_TO_EXTENSION_MAP.put("video/webm", ".webm");

	    // Documents
	    MIME_TYPE_TO_EXTENSION_MAP.put("application/pdf", ".pdf");
	    MIME_TYPE_TO_EXTENSION_MAP.put("application/msword", ".doc");
	    MIME_TYPE_TO_EXTENSION_MAP.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx");
	    MIME_TYPE_TO_EXTENSION_MAP.put("application/vnd.ms-excel", ".xls");
	    MIME_TYPE_TO_EXTENSION_MAP.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx");
	    MIME_TYPE_TO_EXTENSION_MAP.put("application/vnd.ms-powerpoint", ".ppt");
	    MIME_TYPE_TO_EXTENSION_MAP.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", ".pptx");
	    MIME_TYPE_TO_EXTENSION_MAP.put("text/plain", ".txt");
	    MIME_TYPE_TO_EXTENSION_MAP.put("application/rtf", ".rtf");

	    // Compressed/Archive
	    MIME_TYPE_TO_EXTENSION_MAP.put("application/zip", ".zip");
	    MIME_TYPE_TO_EXTENSION_MAP.put("application/x-tar", ".tar");
	    MIME_TYPE_TO_EXTENSION_MAP.put("application/gzip", ".gz");
	    MIME_TYPE_TO_EXTENSION_MAP.put("application/x-7z-compressed", ".7z");
	    MIME_TYPE_TO_EXTENSION_MAP.put("application/x-rar-compressed", ".rar");

	    // Web
	    MIME_TYPE_TO_EXTENSION_MAP.put("text/html", ".html");
	    MIME_TYPE_TO_EXTENSION_MAP.put("text/css", ".css");
	    MIME_TYPE_TO_EXTENSION_MAP.put("application/javascript", ".js");
	    MIME_TYPE_TO_EXTENSION_MAP.put("application/json", ".json");
	    MIME_TYPE_TO_EXTENSION_MAP.put("application/xml", ".xml");

	    // Fonts
	    MIME_TYPE_TO_EXTENSION_MAP.put("font/woff", ".woff");
	    MIME_TYPE_TO_EXTENSION_MAP.put("font/woff2", ".woff2");
	    MIME_TYPE_TO_EXTENSION_MAP.put("application/x-font-ttf", ".ttf");
	    MIME_TYPE_TO_EXTENSION_MAP.put("application/x-font-opentype", ".otf");

	    // Executable (You might want to block these!)
	    MIME_TYPE_TO_EXTENSION_MAP.put("application/x-msdownload", ".exe");
	    MIME_TYPE_TO_EXTENSION_MAP.put("application/x-sh", ".sh");
	    MIME_TYPE_TO_EXTENSION_MAP.put("application/x-dosexec", ".exe");
    }
	
	
    public String getMimeType(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return tika.detect(inputStream);
        } catch(Exception e) {
        	return null;
        }
    }
	
	
    public String getFileExtension(MultipartFile file) {
    	try {
            String mimeType = getMimeType(file);
            return MIME_TYPE_TO_EXTENSION_MAP.get(mimeType);
    	} catch(Exception e) {
    		return null;
    	}
    }
	
	
}
