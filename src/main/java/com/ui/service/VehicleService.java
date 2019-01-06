package com.ui.service;

import com.google.common.collect.ImmutableList;
import com.ui.mvc.model.Vehicle;
import com.ui.mvc.model.VehicleData;
import com.ui.mvc.model.VehicleServiceType;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VehicleService extends AbstractService {

    private List<Map<String, Object>> vehicleList;
    private List<Vehicle> vehicles;
    private List<VehicleServiceType> vehicleServiceTypes;

    private VehicleService(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public static VehicleService of(String user, String password) {
        return new VehicleService(user, password);
    }

    public List<Map<String, Object>> getVehicleList() throws AuthenticationException, IOException {
        if (vehicleList != null) {
            return vehicleList;
        } else {
            fillVehicleList();
            return vehicleList;
        }
    }

    public List<Vehicle> getVehicles() throws IOException, AuthenticationException {
        if (vehicles != null) {
            return vehicles;
        } else {
            fillVehicles();
            return vehicles;
        }

    }

    public Vehicle getVehicleById(int id) throws IOException, AuthenticationException {
        Vehicle vehicle = null;
        for(Vehicle item : getVehicles()) {
            if(item.getId() == id) {
                vehicle = item;
                break;
            }
        }
        if (vehicle == null) {
            throw new RuntimeException("Vehicle not found by id = " +id);
        }
        return vehicle;
    }

    private void fillVehicleList() throws AuthenticationException, IOException {
        List<Map<String, Object>> tempList = new LinkedList<>();
        HttpGet httpGet = new HttpGet(host + "vehicle/vehicles");
        authRequest(httpGet);
        CloseableHttpClient client = HttpClients.createDefault();
        HttpResponse response = client.execute(httpGet);
        if (response.getStatusLine().getStatusCode()== HttpStatus.OK.value()) {
            tempList = Util.getListMapFromEntity(response.getEntity());
        }
        vehicleList = tempList;
        client.close();
    }

    private void fillVehicles() throws IOException, AuthenticationException {

        List<Vehicle> tempList = new LinkedList<>();
        getVehicleList().forEach(map -> {
            int id = (Integer)  map.get("id");
            String description = (String) map.get("description");
            String licensePlate = (String) map.get("licensePlate");
            String vin = (String) map.get("vin");
            String sts = (String) map.get("sts");
            tempList.add(Vehicle.of(id, description, licensePlate, vin, sts));
        });
        vehicles = tempList;
    }

    public VehicleServiceType getVehicleServiceTypeById(int id) {
        return getVehicleServiceTypes().stream().filter(t -> t.getId()==id).collect(Collectors.toList()).get(0);
    }

    public List<VehicleData> getVehicleData(int vehicleId) throws AuthenticationException, IOException {
        HttpGet httpGet = new HttpGet(host + "vehicle/data/last?vehicle="+vehicleId+"&size=100");
        authRequest(httpGet);
        CloseableHttpClient client = HttpClients.createDefault();
        HttpResponse response = client.execute(httpGet);
        if (response.getStatusLine().getStatusCode()== HttpStatus.OK.value()) {
            List<VehicleData> vehicleData = new LinkedList<>();
            Util.getListMapFromEntity(response.getEntity()).forEach(map -> {
                int id = (Integer) map.get("id");
                int serviceTypeId = (Integer) map.get("vehicleServiceType");
                VehicleServiceType serviceType = getVehicleServiceTypeById(serviceTypeId);
                String description = (String) map.get("description");
                int mileAge = (Integer) map.get("mileAge");
                double price = (Double) map.get("price");
                String dateString = (String) map.get("date");
                LocalDate localDate = LocalDate.parse(dateString) ;
                vehicleData.add(VehicleData.of(id, serviceType, vehicleId, description, mileAge, price, localDate));
            });
            return vehicleData;
        }
        client.close();
        return ImmutableList.of();
    }

    private void fillServiceTypes() throws AuthenticationException, IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(host + "vehicle/servicetype");
        authRequest(httpGet);
        HttpResponse response = client.execute(httpGet);
        List<VehicleServiceType> tempList = new LinkedList<>();
        if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
            Util.getListMapFromEntity(response.getEntity()).forEach(map -> {
                int id = (Integer) map.get("id");
                String description = (String) map.get("description");
                tempList.add(VehicleServiceType.of(id, description));
            });
            vehicleServiceTypes = tempList;
        }
        client.close();
    }

    public List<VehicleServiceType> getVehicleServiceTypes() {
        if (vehicleServiceTypes != null) {
            return vehicleServiceTypes;
        } else {
            try {
                fillServiceTypes();
            } catch (AuthenticationException |IOException e) {
                throw new RuntimeException(e);
            }
            return vehicleServiceTypes;
        }
    }

    public void saveVehicleData(VehicleData vehicleData) throws AuthenticationException, IOException {
        String request = host + String.format(
                "vehicle/data/save?type=%d&vehicle=%d&description=%s&mileage=%d&price=%.0f&date=%s",
                vehicleData.getVehicleServiceType().getId(),
                vehicleData.getVehicleId(),
                vehicleData.getDescription().replace(" ", "+"),
                vehicleData.getMileAge(),
                vehicleData.getPrice(),
                vehicleData.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        );
        HttpGet httpGet = new HttpGet(request);
        authRequest(httpGet);
        CloseableHttpClient client = HttpClients.createDefault();
        HttpResponse response = client.execute(httpGet);
        if (!(response.getStatusLine().getStatusCode()==HttpStatus.OK.value())) {
            throw new RuntimeException("Http status not Ok" + EntityUtils.toString(response.getEntity()));
        }
        client.close();
    }
}
