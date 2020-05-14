package com.amazonaws.lambda.demo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.MultipartStream;
import org.json.JSONObject;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

public class LambdaFunctionHandler implements RequestHandler<Object, String> {
 JSONObject request=null;
    @Override
    public String handleRequest(Object input, Context context) {
    	
        context.getLogger().log("Input: " + input);
        request=new JSONObject(""+input);
        System.out.println(request.toString());
        String req=request.getString("audioclip");
        		//input.toString().replace("{content=", "").replace("}", "");
        //request=new JSONObject(""+req);
      //Create the logger
        LambdaLogger logger = context.getLogger();
        logger.log("Loading Java Lambda handler of Proxy new ");
        //Create the APIGatewayProxyResponseEvent response
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        //Set up contentType String
        String contentType = "audio/mpeg";
        
        //Change these values to fit your region and bucket name
        String clientRegion = "ap-south-1";
        String bucketName = "audiocliptest";
        
        //Every file will be named image.jpg in this example. 
        //You will want to do something different here in production
        Random r=new Random();
        String fileObjKeyName = "newaudio"+r.nextInt()+".mp3";   

        try {
        	
        
        	//context.getLogger().log(hps.toString());
            //Get the uploaded file and decode from base64
        	//Map<String, String> hps = event.getHeaders();
            byte[] bI = Base64.decodeBase64(req);
            File file = new File("test.mp3");

            try ( FileOutputStream fos = new FileOutputStream(file); ) {
              // To be short I use a corrupted PDF string, so make sure to use a valid one if you want to preview the PDF file
              String b64 = req;
              byte[] decoder = Base64.decodeBase64(b64);

              fos.write(decoder);
              System.out.println("mp3 File Saved");
            } catch (Exception e) {
              e.printStackTrace();
            }
            //Get the content-type header and extract the boundary
            
            //if (hps != null) {
                //contentType = hps.get("content-type");
            //}
            //String[] boundaryArray = contentType.split("=");
            //
            //Transform the boundary to a byte array
            //byte[] boundary = boundaryArray[1].getBytes();
        	
            //Log the extraction for verification purposes
            logger.log(new String(bI, "UTF-8") + "\n"); 
            
            //Create a ByteArrayInputStream
            ByteArrayInputStream content = new ByteArrayInputStream(bI);
            
            //Create a MultipartStream to process the form-data
           /* MultipartStream multipartStream =
              new MultipartStream(content, bI, bI.length, null);
        	
            //Create a ByteArrayOutputStream
            ByteArrayOutputStream out = new ByteArrayOutputStream();
        	
            //Find first boundary in the MultipartStream
            boolean nextPart = multipartStream.skipPreamble();
            
            //Loop through each segment
            while (nextPart) {
                String header = multipartStream.readHeaders();
                
                //Log header for debugging
                logger.log("Headers:");
                logger.log(header);
                
                //Write out the file to our ByteArrayOutputStream
                multipartStream.readBodyData(out);
                //Get the next part, if any
                nextPart = multipartStream.readBoundary();
            }
            
            //Log completion of MultipartStream processing
            logger.log("Data written to ByteStream");
            */
            //Prepare an InputStream from the ByteArrayOutputStream
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
            s3Client.putObject(bucketName, fileObjKeyName, fis, metadata);
           // PutObjectRequest request = new PutObjectRequest(bucketName, fileObjKeyName, new File(fileName)).withCannedAcl(CannedAccessControlList.PublicRead);

            
            
           System.out.println("URL: "+s3Client.getUrl(bucketName, fileObjKeyName).toExternalForm());   
            //Log status
            logger.log("Put object in S3");

            //Provide a response
            response.setStatusCode(200);
          
        } 
        catch(AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't  
            // process it, so it returned an error response.
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

        logger.log(response.toString());

        // TODO: implement your handler
        return "Hello from Lambda!";
    }

}
