package com.TDI.Services.SMS;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.bson.Document;
import org.json.JSONObject;

import javax.ws.rs.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

/**
 * Created by voodoo on 15/8/16.
 */

@Api(value = "SMS")
@Path("/SMS/")
public class SMS {

    MongoClientURI connectionString = new MongoClientURI("mongodb://voodoo:722446@ds161495.mlab.com:61495/complaint_box");

    MongoClient mongoClient = new MongoClient(connectionString);
    MongoDatabase db = mongoClient.getDatabase(connectionString.getDatabase());
    MongoCollection<Document> collection = db.getCollection("OTP");

    JSONObject obj = new JSONObject();


    public int gen() {
        Random r = new Random( System.currentTimeMillis() );
        int x=100000+r.nextInt(2000000)+ r.nextInt(2000000);
        while(x>999999)
        {
            x=x/10;
        }
        return x;
    }

    public int gen1() {
        Random r = new Random( System.currentTimeMillis() );
        int x=300000+r.nextInt(3000000)+ r.nextInt(8000000)+r.nextInt(5000000);
        while(x>999999)
        {
            x=x/10;
        }
        return x;
    }

    boolean datafound=false;


    @POST
    @Path("/request_OTP")
    @Produces("application/json")
    @ApiOperation(value = "(Mobile number needs to be with country code for eg. +919999999999")
    public String request_OTP(@FormParam("mob_no") String mob_no) {
        /*TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);
        obj = new JSONObject();
        try {
            // Build a filter for the MessageList
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("Body", "testing "));
            params.add(new BasicNameValuePair("To", "+919044848238"));
            params.add(new BasicNameValuePair("From", "+15005550006"));

            MessageFactory messageFactory = client.getAccount().getMessageFactory();
            Message message = messageFactory.create(params);
            System.out.println(message.getSid());
            obj.put("response","success");
            return String.valueOf(obj);
        } catch (Exception e)
        {
            e.printStackTrace();
        }*/

        final String USER_AGENT = "Mozilla/5.0";
        obj = new JSONObject();
        try {

            String OTP=String.valueOf(gen());

            String r_id="Request_"+String.valueOf(gen1());

            String url = "http://smshorizon.co.in/api/sendsms.php?user=sachinnair29&apikey=gRJwMKM8NmlqtUh267sS&mobile="+mob_no+"&senderid=xxyy&message=Your%20ComplaintBox%20Registration%20OTP%20is%20"+OTP+".&type=txt";

            String User_Id="User_"+String.valueOf(gen());

            System.out.println("\n Generated OTP is " + OTP);

           /* URL obj1 = new URL(url);
            System.out.print("url is "+obj1);
            HttpURLConnection con = (HttpURLConnection) obj1.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            //add request header
            con.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = con.getResponseCode();


            //System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            System.out.println("\n response is      : "+response.toString());*/

            int responseCode =200;

            if(responseCode==200)
            {

                datafound=false;
                FindIterable<Document> iterable = collection.find(new org.bson.Document("mob_no", mob_no));
                iterable.forEach(new Block<Document>() {
                    @Override
                    public void apply(final org.bson.Document document) {
                        datafound=true;
                    }

                });

                if(datafound==true)
                {
                    UpdateResult ur = collection.updateOne(new org.bson.Document("mob_no", mob_no), new org.bson.Document("$set", new org.bson.Document("verification_OTP", OTP).append("request_id",r_id)));
                    if (ur.getModifiedCount() != 0) {
                        obj.put("response","success");
                        obj.put("request_id",r_id);
                        return String.valueOf(obj);
                    }

                    obj.put("response","failure");
                    return String.valueOf(obj);
                }else
                {
                    org.bson.Document doc1 = new org.bson.Document("mob_no", mob_no)
                            .append("verification_OTP", OTP).append("request_id",r_id);
                    collection.insertOne(doc1);
                    obj.put("response","success");
                    obj.put("request_id",r_id);
                    return String.valueOf(obj);
                }
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        obj.put("response","failure");
        return String.valueOf(obj);
    }


    String OTP;
    @GET
    @Path("/verify_OTP")
    @Produces("application/json")
    @ApiOperation(value = "(Dont!! call this method..its called internally")
    public String verify_OTP(@QueryParam("mob_no") String mob_no,@QueryParam("request_id") String request_id,@QueryParam("verification_OTP") String verification_OTP) {
        datafound=false;

        try {
            datafound=false;
            OTP=null;
            System.out.println("matching "+mob_no+" "+request_id+" "+OTP);
            FindIterable<org.bson.Document> iterable = collection.find(new org.bson.Document("mob_no", mob_no).append("request_id",request_id));
            iterable.forEach(new Block<org.bson.Document>() {
                @Override
                public void apply(final org.bson.Document document) {
                    datafound=true;
                    OTP = String.valueOf(document.get("verification_OTP"));

                }

            });
            if(datafound==true && OTP.equals(verification_OTP)){
                System.out.println("Verification Success");
                obj.put("response","valid");
                return String.valueOf(obj);
            }
            else
            {
                System.out.println("Verification failed");
                obj.put("response","invalid");
                return String.valueOf(obj);
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        System.out.println("Verification failed11");
        obj.put("response","invalid");
        return String.valueOf(obj);

    }


    @GET
    @Path("/delete_OTP")
    @Produces("application/json")
    @ApiOperation(value = "(Dont!! call this method..its called internally")
    public String delete_OTP(@QueryParam("mob_no") String mob_no,@QueryParam("request_id") String request_id,@QueryParam("verification_OTP") String verification_OTP) {
        datafound=false;

        try {
            datafound=false;
            OTP=null;

            DeleteResult dr = collection.deleteMany(new org.bson.Document("mob_no", mob_no).append("request_id", request_id));
            if (dr.getDeletedCount() != 0) {
                obj.put("response","true");
                return String.valueOf(obj);
            }
            else
            {
                obj.put("response","false");
                return String.valueOf(obj);
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        obj.put("response", "false");
        return String.valueOf(obj);

    }



}
