package com.ui.service;

import com.ui.mvc.model.property.PropertyData;
import com.ui.mvc.model.property.PropertyType;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class PropertyService extends AbstractService {

    private List<PropertyType> propertyList;
    private List<PropertyType> serviceList;

    public PropertyService(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public List<PropertyType> getPropertyList() throws IOException, AuthenticationException {
        if (propertyList != null) {
            return propertyList;
        } else {
            fillPropertyList();
            return propertyList;
        }
    }

    private void fillPropertyList() throws AuthenticationException, IOException {
        List<PropertyType> tempList = new LinkedList<>();
        HttpGet httpGet = new HttpGet(host + "property/properties");
        authRequest(httpGet);
        CloseableHttpClient client = HttpClients.createDefault();
        HttpResponse response = client.execute(httpGet);
        if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
            tempList = getPropertiesFromEntity(response.getEntity());
        }
        propertyList = tempList;
        client.close();
    }

    public List<PropertyType> getServiceList() throws IOException, AuthenticationException {
        if (serviceList != null) {
            return serviceList;
        } else {
            fillServiceList();
            return serviceList;
        }
    }

    private void fillServiceList() throws AuthenticationException, IOException {
        List<PropertyType> tempList = new LinkedList<>();
        HttpGet httpGet = new HttpGet(host + "property/types");
        authRequest(httpGet);
        CloseableHttpClient client = HttpClients.createDefault();
        HttpResponse response = client.execute(httpGet);
        if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
            tempList = getPropertiesFromEntity(response.getEntity());
        }
        serviceList = tempList;
        client.close();
    }

    public List<PropertyType> getPropertiesFromEntity(HttpEntity httpEntity) throws IOException {
        String jsonString = EntityUtils.toString(httpEntity);
        JSONArray jsonArray = new JSONArray(jsonString);
        List<PropertyType> propertyTypes = new LinkedList<>();
        jsonArray.forEach(t -> {
            JSONObject json = (JSONObject) t;
            PropertyType type = new PropertyType(json.getInt("id"), json.getString("description"));
            propertyTypes.add(type);
        });
        return propertyTypes;
    }

    public List<PropertyData> getPropertyDataFromEntity(HttpEntity httpEntity) throws IOException {
        String jsonString = EntityUtils.toString(httpEntity);
        JSONArray jsonArray = new JSONArray(jsonString);
        List<PropertyData>  propertyDataList = new LinkedList<>();
        jsonArray.forEach(t -> {
            JSONObject json = (JSONObject) t;
            JSONObject jsonProperty = json.getJSONObject("property");
            JSONObject jsonService =  json.getJSONObject("propertyServiceType");
            PropertyType property = getTypeFromJson(jsonProperty);
            PropertyType service = getTypeFromJson(jsonService);
            String dateString = json.getString("updateDate");
            LocalDate updateDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            PropertyData data = new PropertyData(
                    json.getLong("id"),
                    property,
                    service,
                    json.getString("description"),
                    json.getString("name"),
                    json.getDouble("price"),
                    updateDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            );
            propertyDataList.add(data);
        });
        return propertyDataList;
    }

    private PropertyType getTypeFromJson(JSONObject jsonObject) {
        int id = jsonObject.getInt("id");
        String description = jsonObject.getString("description");
        return new PropertyType(id, description);
    }

    public List<PropertyData> getPropertyData(
            int propertyId,
            String description,
            String start, String end
    ) throws AuthenticationException, IOException {
        List<PropertyData> tempList = new LinkedList<>();
        HttpPost httpPost = new HttpPost(host + "property/data");
        String body = String.format(
                "{\"property\":{\"id\":%d,\"description\":\"%s\"},\"startDate\":\"%s\",\"endDate\":\"%s\"}",
                propertyId,
                description,
                start,
                end
                );
        httpPost.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        authRequest(httpPost);
        CloseableHttpClient client = HttpClients.createDefault();
        HttpResponse response = client.execute(httpPost);
        if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
            tempList = getPropertyDataFromEntity(response.getEntity());
        }
        client.close();
        return tempList;
    }

    public void savePropertyData(
            PropertyData propertyData
    ) throws AuthenticationException, IOException {
        String template = "{\"property\":{\"id\":%d,\"description\":\"%s\"}," +
                "\"propertyServiceType\":{\"id\":%d,\"description\":\"%s\"}," +
                "\"description\":\"%s\",\"name\":\"%s\"," +
                "\"price\":%.0f,\"updateDate\":\"%s\"}";
        String body = String.format(
                template,
                propertyData.getProperty().getId(),
                propertyData.getProperty().getDescription(),
                propertyData.getService().getId(),
                propertyData.getService().getDescription(),
                propertyData.getDescription(),
                propertyData.getName(),
                propertyData.getPrice(),
                propertyData.getUpdateDate()
                );

        HttpPost httpPost = new HttpPost(host + "property/data/save");
        httpPost.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        authRequest(httpPost);
        CloseableHttpClient client = HttpClients.createDefault();
        HttpResponse response = client.execute(httpPost);
        if (response.getStatusLine().getStatusCode() != HttpStatus.OK.value()) {
            throw new RuntimeException("Save data failed: " + response.getStatusLine().toString());
        }
        client.close();
    }

    public PropertyType getPropertyById(int id) throws IOException, AuthenticationException {
        return getPropertyList().stream().filter(t -> t.getId() == id).collect(Collectors.toList()).get(0);
    }

    public PropertyType getServiceById(int id) throws IOException, AuthenticationException {
        return getServiceList().stream().filter(t -> t.getId() == id).collect(Collectors.toList()).get(0);
    }


}
