package com.TDI.Services.SMS;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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

    @GET
    @Path("/sms_test")
    @Produces("application/json")
    @ApiOperation(value = "(Dont call this api. Just test here). The \"To\" field needs to be with country code for eg. +919999999999")
    public String sms_test(@QueryParam("to") String to) {
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
            String url = "http://smshorizon.co.in/api/sendsms.php?user=sachinnair29&apikey=gRJwMKM8NmlqtUh267sS&mobile="+to+"&senderid=xxyy&message=Your%20ComplaintBox%20Registration%20OTP%20is%20"+OTP+".&type=txt";

            URL obj1 = new URL(url);
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
            System.out.println("\n response is      : "+response.toString());
            if(responseCode==200)
            {

                obj.put("response","success");
                return String.valueOf(obj);
            }
            //print result

        }catch (Exception e)
        {
            e.printStackTrace();
        }






        obj.put("response","failure");
        return String.valueOf(obj);
    }

}
