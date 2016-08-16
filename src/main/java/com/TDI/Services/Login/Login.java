package com.TDI.Services.Login;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.net.URL;
import java.util.Random;

import com.TDI.Services.SMS.SMS;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.bson.Document;
import org.json.JSONObject;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Message;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import java.util.ArrayList;
import java.util.List;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by voodoo on 15/8/16.
 */

@Api(value = "Login")
@Path("/Login/")
public class Login {

    MongoClientURI connectionString = new MongoClientURI("mongodb://voodoo:722446@ds161495.mlab.com:61495/complaint_box");

    MongoClient mongoClient = new MongoClient(connectionString);
    MongoDatabase db = mongoClient.getDatabase(connectionString.getDatabase());
    MongoCollection<Document> collection = db.getCollection("credentials");
    JSONObject obj = new JSONObject();





    public int gen() {
        Random r = new Random( System.currentTimeMillis() );
        return 10000 + r.nextInt(20000);
    }


    String tou,pwd;
    int is_valid=0;

    @GET
    @Path("/validate_user")
    @Produces("application/json")
    @ApiOperation(value = "check if user is valid or invalid. The method will return a json object with response \"valid\" if correct and \"invalid\" for incorrect details")
    public String validate_user(@QueryParam("user_name") String user_name,@QueryParam("password") String password) {
        //       public String validate_user(@FormParam("user_name") String user_name,@FormParam("password") String password) {
        obj = new JSONObject();
        try {
            is_valid=0;
            pwd=null;
            tou=null;
            FindIterable<org.bson.Document> iterable = collection.find(new org.bson.Document("user_name", user_name));
            iterable.forEach(new Block<org.bson.Document>() {
                @Override
                public void apply(final org.bson.Document document) {
                    is_valid=1;
                    pwd = String.valueOf(document.get("password"));
                    tou = String.valueOf(document.get("type_of_user"));
                }

            });
            if(is_valid==1 && pwd.equals(password)){
                obj.put("type_of_user", tou);
                obj.put("response","valid");

                return String.valueOf(obj);
            }
            else
            {
                obj.put("response","invalid");
                return String.valueOf(obj);
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        obj.put("response","invalid");
        return String.valueOf(obj);
    }





  Boolean datafound=false;
    String OTP;

    @POST
    @Path("/register_user")
    @Produces("application/json")
    public String register_user(@FormParam("name") String name,@FormParam("dob") String dob,@FormParam("user_name") String user_name,
                                @FormParam("password") String password,@FormParam("email")String email,@FormParam("address")String address,@FormParam("m_no")String m_no,@FormParam("l_no")String l_no,@FormParam("request_id")String request_id,@FormParam("verification_OTP") String verification_OTP) {



        datafound=false;
        obj = new JSONObject();

        try {
           SMS sms_obj=new SMS();
           obj= new JSONObject(sms_obj.verify_OTP(m_no,request_id,verification_OTP));

            if(obj.get("response").equals("invalid"))
            {
                obj = new JSONObject();
                obj.put("status", "invalid");
                return String.valueOf(obj);
            }

            obj = new JSONObject();
            is_valid=0;
            FindIterable<org.bson.Document> iterable = collection.find(new org.bson.Document("user_name", user_name));
            iterable.forEach(new Block<org.bson.Document>() {
                @Override
                public void apply(final org.bson.Document document) {
                    is_valid=1;
                }

            });


            if(is_valid==1)
            {
                obj.put("status", "false");
                return String.valueOf(obj);
            }



            Document doc;

                doc = new org.bson.Document("name", name)
                        .append("dob", dob)
                        .append("user_name", user_name)
                        .append("password", password)
                        .append("email", email)
                        .append("address", address)
                        .append("m_no", m_no)
                        .append("m_no", m_no)
                        .append("l_no", l_no)
                        .append("type_of_user", "normal");



            collection.insertOne(doc);

            sms_obj.delete_OTP(m_no, request_id, verification_OTP);

            obj.put("status", "true");
            return String.valueOf(obj);

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        obj.put("status", "false");
        return String.valueOf(obj);
    }


    @GET
    @Path("/username_availability_check")
    @Produces("application/json")
    public String username_availability_check(@QueryParam("user_name") String user_name) {
        obj = new JSONObject();
        System.out.println("checking " + user_name);
        try {
            is_valid=0;
            FindIterable<org.bson.Document> iterable = collection.find(new org.bson.Document("user_name", user_name));
            iterable.forEach(new Block<org.bson.Document>() {
                @Override
                public void apply(final org.bson.Document document) {
                    is_valid=1;
                }

            });
            if(is_valid==1)
            {
                obj.put("response", "false");
                return String.valueOf(obj);
            }
            obj.put("response", "true");
            return String.valueOf(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        obj.put("response", "false");
        return String.valueOf(obj);
    }

}

