package com.ui.service;

import com.ui.mvc.model.Expenses;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class DataConverter {

    public static Expenses convertToExpenses(Map<String, Object> map) {

        String stringId = ((Integer) map.get("id")).toString();
        long id = Long.valueOf(stringId);
        int month = (Integer) map.get("month");
        int year =  (Integer) map.get("year");
        String type = (String) map.get("type");
        String description = (String) map.get("description");
        String date = (String) map.get("date");
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String dateString = localDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        double amount = (Double) map.get("amount");

        return Expenses.of(id, month, year, type, description, dateString, amount);
    }
}
