package com.ui.mvc.controller;

import com.ui.mvc.model.VehicleData;
import com.ui.mvc.model.VehicleServiceType;
import com.ui.service.VehicleService;
import org.apache.http.auth.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
public class VehicleController extends AbstractController{

    private VehicleService vehicleService;

    public VehicleController() {
        this.vehicleService = VehicleService.of(USER, PASSWORD);
    }

    @RequestMapping(value = "/vehicle/", method = RequestMethod.GET)
    public String vehicle(Model model) throws IOException, AuthenticationException {
        model.addAttribute("vehicles", vehicleService.getVehicleList());
        return "vehicle/vehicle";
    }

    @RequestMapping(value = "vehicle/details", method = RequestMethod.GET)
    public String vehicleDetails(Model model, @RequestParam(value="vehicle") String vehicleId) throws IOException, AuthenticationException {
        int id = Integer.parseInt(vehicleId);
        model.addAttribute("vehicle", vehicleService.getVehicleById(id));
        model.addAttribute("vehicleData", vehicleService.getVehicleData(id));
        return "vehicle/vehicle_details";
    }

    @RequestMapping(value = "vehicle/data/add", method = RequestMethod.GET)
    public String vehicleDetailsAdd(Model model, @RequestParam int vehicleId) {
        model.addAttribute("services", vehicleService.getVehicleServiceTypes());
        model.addAttribute("vehicleId", vehicleId);
        return "vehicle/vehicle_data_add";
    }

    @RequestMapping(value = "/vehicle/data/add/response", method = RequestMethod.GET)
    public String vehicleAddDetailsResponse(
            Model model,
            @RequestParam int vehicleId,
            @RequestParam int service,
            @RequestParam String description,
            @RequestParam int mileAge,
            @RequestParam double price,
            @RequestParam String date
    ) throws IOException, AuthenticationException {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        VehicleServiceType serviceType = vehicleService.getVehicleServiceTypeById(service);
        VehicleData vehicleData = VehicleData.of(serviceType, vehicleId, description, mileAge, price, localDate);
        model.addAttribute("vehicleData", vehicleData);
        vehicleService.saveVehicleData(vehicleData);
        return "vehicle/vehicle_data_add_response";
    }

}
