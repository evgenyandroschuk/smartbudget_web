package com.ui.service;

import com.ui.mvc.model.Expenses;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Util {

    public static List<Map<String, Object>> getListMapFromEntity(HttpEntity httpEntity) throws IOException {
        String jsonString = EntityUtils.toString(httpEntity);
        JSONArray jsonArray = new JSONArray(jsonString);
        List list =  jsonArray.toList();
        return list;
    }

    public static JSONObject getJSONObjectFromEntity(HttpEntity httpEntity) throws IOException {
        String jsonString = EntityUtils.toString(httpEntity);
        return new JSONObject(jsonString);
    }

    public static double parseAmountString(String s) {

        double amount = 0;
        double amtPlus;

        String firstSimbol = s.substring(0,1);

        if(firstSimbol.equals("=")){

            String sNumber = "";

            char [] myChar = s.toCharArray ();
            for (int i=0 ; i<myChar.length;i++ ){


                if(Character.isDigit(myChar[i])==true && i!=myChar.length){

                    String chStr = Character.toString(myChar[i]);

                    sNumber = sNumber.concat(chStr);

                }
                if(Character.toString(myChar[i]).equals("-")){

                    amtPlus = Double.parseDouble(sNumber);
                    amount = amount+amtPlus;

                    sNumber = "-";

                }
                if(Character.toString(myChar[i]).equals("+")){

                    amtPlus = Double.parseDouble(sNumber);
                    amount = amount+amtPlus;
                    sNumber = "";

                }
            }

            amtPlus = Double.parseDouble(sNumber);
            amount = amount+amtPlus;

        } else{
            amount = Double.parseDouble(s);
        }
        return amount;
    }
}
