package com.ui.mvc.controller;

import com.ui.mvc.model.property.PropertyData;
import com.ui.mvc.model.property.PropertyType;
import com.ui.service.PropertyService;
import org.apache.http.auth.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class PropertyController extends AbstractController {

    private PropertyService propertyService;

    public PropertyController() {
        this.propertyService = new PropertyService(USER, PASSWORD);
    }

    @RequestMapping(value = "/communication", method = RequestMethod.GET)
    public String properties(Model model) throws IOException, AuthenticationException {
        model.addAttribute("properties", propertyService.getPropertyList());
        return "property/property";
    }

    @RequestMapping(value = "/property/details", method = RequestMethod.GET)
    public String propertyData(Model model, @RequestParam(value="property") Integer propertyId) throws IOException, AuthenticationException {
        PropertyType property = propertyService.getPropertyById(propertyId);
        LocalDate start = LocalDate.parse("2011-01-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate end = LocalDate.now().plusDays(1);
        List<PropertyData> dataList = propertyService.getPropertyData(
                property.getId(),
                property.getDescription(),
                start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                );
        model.addAttribute("propertyData", dataList);
        model.addAttribute("property", property);
        return "property/property_details";
    }

    @RequestMapping(value = "/property/data/add", method = RequestMethod.GET)
    public String savePropertyData(Model model, @RequestParam int propertyId) throws IOException, AuthenticationException {
        model.addAttribute("services", propertyService.getServiceList());
        model.addAttribute("property", propertyService.getPropertyById(propertyId));
        return "property/property_data_add";
    }

    @RequestMapping(value = "/property/data/add/response", method = RequestMethod.GET)
    public String savePropertyDataResponse(
            Model model,
            @RequestParam int propertyId,
            @RequestParam int serviceId,
            @RequestParam String description,
            @RequestParam String name,
            @RequestParam double price,
            @RequestParam String date
    ) throws IOException, AuthenticationException {
        PropertyType property = propertyService.getPropertyById(propertyId);
        PropertyType service = propertyService.getServiceById(serviceId);
        PropertyData propertyData = new PropertyData(property, service, description, name, price, date);
        model.addAttribute("propertyData", propertyData);
        model.addAttribute("property", property);
        propertyService.savePropertyData(propertyData);
        return "property/property_data_add_response";
    }

}
