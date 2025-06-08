package forge.services;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;

import forge.consts.Consts_DiscussionBoard;
import net.coobird.thumbnailator.Thumbnails;

@Service
public class FileStorageService {
	
	
	private final int MAX_IMAGE_WIDTH = 600;
	
	private final MimeTypeCheckService mimeTypeCheckService;
	
	@Autowired
	public FileStorageService(MimeTypeCheckService mimeTypeCheckService) {
		this.mimeTypeCheckService = mimeTypeCheckService;
	}
	
	
    public boolean saveFile(String relativeFilePath, MultipartFile file) {	// just save file directly
    	
    	try 
    	{	
    		Path currentDir 		= Paths.get("").toAbsolutePath();
    		Path filePath 			= currentDir.resolve(relativeFilePath);
    		Path parentDirectory 	= filePath.getParent();

            if (!Files.exists(parentDirectory)) {
                Files.createDirectories(parentDirectory);  
            }

            file.transferTo(filePath.toFile());  
            return true;
    	}
    	catch(Exception e) 
    	{
    		e.printStackTrace();
    		return false;
    	}
    	
    }
    
    
    public boolean deleteFile(String relativeFilePath) {	// just delete file directly
    	
    	try 
    	{
    		Path filePath = Paths.get(relativeFilePath);
    		
            if (Files.exists(filePath)) {
                Files.delete(filePath);  
            } else {
            	System.out.println("File not found : " + relativeFilePath);
            }

            return true;
    	}
    	catch(Exception e) 
    	{
    		e.printStackTrace();
    		return false;
    	}

    }
    
    
	public boolean saveImageFile(String relativeFilePath, MultipartFile file) {	// save JPG, PNG, GIF with Filtering
		
		try
		{
			String fileType = mimeTypeCheckService.getFileExtension(file);
			
			if
			(	
				fileType.equals(Consts_DiscussionBoard.ALLOWED_IMG_MIME_TYPE_JPG) || 
				fileType.equals(Consts_DiscussionBoard.ALLOWED_IMG_MIME_TYPE_PNG)	
			) 
			{
				saveStatic(relativeFilePath, file, fileType);
			} 
			else if(fileType.equals(Consts_DiscussionBoard.ALLOWED_IMG_MIME_TYPE_GIF)) 
			{
				saveFile(relativeFilePath, file);
			}
			
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		
	}
	

	private boolean saveStatic(String relativeFilePath, MultipartFile inputFile, String fileFormat) {
		
		FileOutputStream fos = null;
		
		try
		{
    		Path currentDir 			= 	Paths.get("").toAbsolutePath();
    		Path filePath 				= 	currentDir.resolve(relativeFilePath);
    		Path parentDirectory 		= 	filePath.getParent();
    		
            if (!Files.exists(parentDirectory)) {
                Files.createDirectories(parentDirectory);  
            }
    		
    		File outputFile = 	new File(filePath.toString());
    		fos = new FileOutputStream(outputFile);
            fos.write(getFilteredStatic(inputFile, fileFormat));
			
			return true;
		}
		catch(Exception e)
		{
    		e.printStackTrace();
    		return false;
		}
		finally
		{
			try {
				if(fos != null) fos.close();
			} catch(IOException ex) {
				ex.printStackTrace();
			} catch(Exception ee) {
				ee.printStackTrace();
			}
		}
		
	}
	
	
	private byte[] getFilteredStatic(MultipartFile inputFile, String fileFormat) throws IOException, MetadataException {
		

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        BufferedImage image = ImageIO.read(inputFile.getInputStream());
	        Metadata metadata 	= getMetadata(inputFile.getInputStream());
	        int orientation 	= getOrientation(metadata);
			
	        int angle = 0;
	        if(orientation == 6) {
	        	angle = 90;
	        } else if(orientation == 3) {
	        	angle = 180;
	        } else if(orientation == 8) {
	        	angle = 270;
	        }
	        
	        Thumbnails.of(image)								// remove metadata automatically when you do Thumbnails.of( ~ )
	                  .rotate(angle)
	                  .width(MAX_IMAGE_WIDTH)
	                  .keepAspectRatio(true) 					// Keep the same ratio
	                  .outputFormat(fileFormat.substring(1))
	                  .toOutputStream(baos);
	        

	        return baos.toByteArray();
	}
	
	
    
    public Metadata getMetadata(InputStream inputStream) {
        Metadata metadata;

        try {
            metadata = ImageMetadataReader.readMetadata(inputStream);
        } catch (ImageProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return metadata;
    }
    
    public Integer getOrientation(Metadata metadata) throws MetadataException {
        int orientation = 1;

        Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

        if(directory != null && directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION))  {	// check directory and orientation value inside
            orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
        }

        return orientation;
    }
    
    
    
    
    /* 
    	
    	
   	사용 X. 참조만.
    	
    public BufferedImage rotateImage (BufferedImage bufferedImage, int orientation) {
    	
        BufferedImage rotatedImage;

        if(orientation == 6 ) {
            rotatedImage = Scalr.rotate(bufferedImage, Scalr.Rotation.CW_90);
        } else if (orientation == 3) {
            rotatedImage = Scalr.rotate(bufferedImage, Scalr.Rotation.CW_180);
        } else if(orientation == 8) {
            rotatedImage = Scalr.rotate(bufferedImage, Scalr.Rotation.CW_270);
        } else {
            rotatedImage = bufferedImage;
        }

        return rotatedImage;
    }
    
    */

}


