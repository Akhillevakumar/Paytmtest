package com.example.akhil.paytm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
EditText mobile;
Button pay;
String st;
String amt;
String custid;
String orderId;
String checsum;

public static final String MID="xxxxxx06662714969544";
public static final String INDUSTRY_TYPE_ID="Retail";
public static final String CHANNEL_ID="WAP";
public static final String WEBSITE="APPSTAGING";
public static final String CALLBACK_URL="https://securegw.paytm.in/theia/paytmCallback";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
mobile=(EditText) findViewById(R.id.editmobile);
pay=(Button)findViewById(R.id.pay);

pay.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
st=mobile.getText().toString().trim();
if(st.length()==10){
    GenerateCheck();
}
else {
    Toast.makeText(MainActivity.this, "Enter mobile number", Toast.LENGTH_SHORT).show();
}
    }
});
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }
    @Override
    protected void onStart() {
        super.onStart();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }
    private void GenerateCheck() {
        Random r = new Random(System.currentTimeMillis());
         orderId = "ORDER" + (1 + r.nextInt(2)) * 10000
                + r.nextInt(10000);
    String url="https://whiteoval.000webhostapp.com/checksum.php";
        Map<String,String>params=new HashMap<String, String>();
        params.put("MID",MID);
        params.put("ORDER_ID",orderId);
        params.put("CUST_ID",custid);
        params.put("INDUSTRY_TYPE_ID",INDUSTRY_TYPE_ID);
        params.put("CHANNEL_ID",CHANNEL_ID);
        params.put("TXN_AMOUNT",amt);
        params.put("WEBSITE",WEBSITE);
        params.put("CALLBACK_URL",CALLBACK_URL);
        params.put("MOBILE_NO",st);

        JSONObject param= new JSONObject(params);
        JsonObjectRequest jsonObjectRequest =new JsonObjectRequest(Request.Method.POST, url, param, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                checsum=response.optString("CHECKSUMHASH");
                if(checsum.trim().length()!=0){
                        onStartTransaction();
                }

                //  Toast.makeText(MainActivity.this, ""+response, Toast.LENGTH_SHORT).show();
                Log.e("getresponce", String.valueOf(response));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Error"+error, Toast.LENGTH_SHORT).show();
                Log.e("ERROR", String.valueOf(error));
                error.printStackTrace();
            }
        });
Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    public void onStartTransaction() {
        PaytmPGService Service = PaytmPGService.getStagingService();
        Map<String, String> paramMap = new HashMap<String, String>();

        // these are mandatory parameters

        paramMap.put("CALLBACK_URL",CALLBACK_URL);
        paramMap.put("CHANNEL_ID",CHANNEL_ID);
        paramMap.put("CHECKSUMHASH",checsum);
        paramMap.put("CUST_ID",custid);
        paramMap.put("INDUSTRY_TYPE_ID",INDUSTRY_TYPE_ID);
        paramMap.put("MID",MID);
        paramMap.put("ORDER_ID",orderId);
        paramMap.put("TXN_AMOUNT",amt);
        paramMap.put("WEBSITE",WEBSITE);


///////////ERROR/////////////
        PaytmOrder Order = new PaytmOrder((HashMap<String, String>) paramMap);


        Service.initialize(Order, null);

        Service.startPaymentTransaction(this, true, true,
                new PaytmPaymentTransactionCallback() {
                    @Override
                    public void someUIErrorOccurred(String inErrorMessage) {

                    }



                    @Override
                    public void onTransactionResponse(Bundle inResponse) {
                        Log.d("LOG", "Payment Transaction is successful " + inResponse);
                        Toast.makeText(getApplicationContext(), "Payment Transaction response " + inResponse.toString(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void networkNotAvailable() { // If network is not
                        // available, then this
                        // method gets called.
                    }

                    @Override
                    public void clientAuthenticationFailed(String inErrorMessage) {

                    }

                    @Override
                    public void onErrorLoadingWebPage(int iniErrorCode,
                                                      String inErrorMessage, String inFailingUrl) {

                    }

                    // had to be added: NOTE
                    @Override
                    public void onBackPressedCancelTransaction() {
                        Toast.makeText(MainActivity.this,"Back pressed. Transaction cancelled",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
                        Log.d("LOG", "Payment Transaction Failed " + inErrorMessage);
                        Toast.makeText(getBaseContext(), "Payment Transaction Failed ", Toast.LENGTH_LONG).show();
                    }

                });
    }
}

