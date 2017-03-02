package org.hadatac.console.http;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import play.mvc.Http.Request;

public class ResumableUpload {
    public static boolean uploadFileByChunking(Request request, String baseDir) {
    	int nResumableChunkNumber = getResumableChunkNumber(request);
        ResumableInfo info = getResumableInfo(request, baseDir);

        if (info.uploadedChunks.contains(new ResumableInfo.ResumableChunkNumber(nResumableChunkNumber))) {
        	return true;
        }
        
        return false;
    }

    public static boolean postUploadFileByChunking(Request request, String baseDir) {
        int nResumableChunkNumber = getResumableChunkNumber(request);
        ResumableInfo info = getResumableInfo(request, baseDir);
		try {
			RandomAccessFile raf = new RandomAccessFile(info.resumableFilePath, "rw");
			//Seek to position
	        raf.seek((nResumableChunkNumber - 1) * (long)info.resumableChunkSize);
	        //Save to file
	        byte[] bytes = request.body().asRaw().asBytes();
	        if (bytes != null) {
	        	int read = 0;
		        int write_size = 950 * 100;
		        while (read < bytes.length) {
		        	raf.write(bytes, read, Math.min(write_size, bytes.length - read));
		        	read += Math.min(write_size, bytes.length - read);
		        }
	        }
	        raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

        info.uploadedChunks.add(new ResumableInfo.ResumableChunkNumber(nResumableChunkNumber));
        if (info.checkIfUploadFinished()) { //Check if all chunks uploaded, and change filename
            ResumableInfoStorage.getInstance().remove(info);
            return true;
        } else {
        	return false;
        }
    }
    
    private static int getResumableChunkNumber(Request request) {
    	return HttpUtils.toInt(request.getQueryString("resumableChunkNumber"), -1);
    }

    private static ResumableInfo getResumableInfo(Request request, String base_dir) {
        int resumableChunkSize = HttpUtils.toInt(request.getQueryString("resumableChunkSize"), -1);
        long resumableTotalSize = HttpUtils.toLong(request.getQueryString("resumableTotalSize"), -1);
        String resumableIdentifier = request.getQueryString("resumableIdentifier");
        String resumableFilename = request.getQueryString("resumableFilename");
        String resumableRelativePath = request.getQueryString("resumableRelativePath");
        
        //Here we add a ".temp" to every upload file to indicate NON-FINISHED
        File folder = new File(base_dir);
 		if (!folder.exists()){
 			folder.mkdirs();
 	    }
        String resumableFilePath = new File(base_dir, resumableFilename).getAbsolutePath() + ".temp";
        ResumableInfoStorage storage = ResumableInfoStorage.getInstance();
        ResumableInfo info = storage.get(resumableChunkSize, resumableTotalSize,
                resumableIdentifier, resumableFilename, resumableRelativePath, resumableFilePath);
        if (!info.vaild())         {
            storage.remove(info);
        }
        
        return info;
    }
}

