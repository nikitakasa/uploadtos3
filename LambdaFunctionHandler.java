package com.amazonaws.lambda.demo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.json.JSONObject;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.Base64;

public class LambdaFunctionHandler implements RequestHandler<Object, String>{
	JSONObject request=null;
    @Override
    public String handleRequest(Object input, Context context) {
    	
        context.getLogger().log("NEW Input: " + input);
        String req=input.toString().replace("{audioclip=", "").replace("}", "");
        LambdaLogger logger = context.getLogger();
        logger.log("Loading Java Lambda handler of Proxy new ");
        String contentType = "audio/mpeg";
        String clientRegion = "ap-south-1";
        String bucketName = "audiocliptest";
        Random r=new Random();
        String fileObjKeyName = "newaudio"+r.nextInt()+".mp3";   

        try {
        	
        	byte[] bI = Base64.decode(req);
            logger.log(new String(bI, "UTF-8") + "\n"); 
            ByteArrayInputStream content = new ByteArrayInputStream(bI);
            InputStream fis = new ByteArrayInputStream(bI);
        	
            //Create our S3Client Object
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(clientRegion)
                    .build();
    
            //Configure the file metadata
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(bI.length);
            metadata.setContentType("audio/mpeg");
            metadata.setCacheControl("public, max-age=31536000");
            
            //Put file into S3
            s3Client.putObject(new PutObjectRequest(bucketName, fileObjKeyName, fis, metadata)
    				.withCannedAcl(CannedAccessControlList.PublicRead));

            
            
           System.out.println("URL: "+s3Client.getUrl(bucketName, fileObjKeyName).toExternalForm());   
            //Log status
            logger.log("Put object in S3");
            return "URL: "+s3Client.getUrl(bucketName, fileObjKeyName).toExternalForm();
        } 
        catch(AmazonServiceException e) {
            logger.log(e.getMessage());
        }
        catch(SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            logger.log(e.getMessage());
        } 
        catch (IOException e) {
            // Handle MultipartStream class IOException
            logger.log(e.getMessage());
        }

        //logger.log(response.toString());

        return "Hello from Lambda!";
    }

}
