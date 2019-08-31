package com.ui.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.ui.mvc.model.Expenses;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ExpensesService extends AbstractService {

    private final int USER_ID = 1;
    private final String EXPENSES_TYPE = "expensesType";
    private List<Map<String, Object>> expensesTypeList;

    private ExpensesService(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public static ExpensesService of(String user, String password) {
        return new ExpensesService(user, password);
    }

    public List<Expenses> getCurrentMonthExpenses() throws AuthenticationException, IOException {

        LocalDate now = LocalDate.now();
        HttpGet httpGet =
                new HttpGet(String.format("http://localhost:7004/budget/v2/expenses?user_id=%d&month=%d&year=%d", USER_ID, now.getMonthValue(), now.getYear()));
        authRequest(httpGet);
        HttpResponse response = client.execute(httpGet);

        if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
            return getExpensesByEntity(response.getEntity());
        }

        System.out.println(response.getStatusLine());
        return ImmutableList.of();
    }

    public List<Expenses> getExpensesByYearMonth(int year, int month) throws AuthenticationException, IOException {
        String requestPattern = "http://localhost:7004/budget/v2/expenses?user_id=%d&year=%d&month=%d";
        String requestString = String.format(requestPattern, USER_ID, year, month);
        HttpGet httpGet = new HttpGet(requestString);
        authRequest(httpGet);
        HttpResponse response = client.execute(httpGet);

        if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
            return getExpensesByEntity(response.getEntity());
        }

        System.out.println(response.getStatusLine());
        return ImmutableList.of();
    }

    public List<Expenses> getExpensesByDescription(
            String description, String startDate, String endDate
    ) throws AuthenticationException, IOException {
        String desc = description.replace(" ", "+");
        String requestPattern = "http://localhost:7004/budget/v2/expenses/description?user_id=%d&description=%s&start=%s&end=%s";
        String requestString = String.format(requestPattern, USER_ID, desc, startDate, endDate);
        HttpGet httpGet = new HttpGet(requestString);
        authRequest(httpGet);
        HttpResponse response = client.execute(httpGet);

        if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
            return getExpensesByEntity(response.getEntity());
        }

        System.out.println(response.getStatusLine());
        return ImmutableList.of();
    }

    private List<Expenses> getExpensesByEntity(HttpEntity entity) throws IOException {
        List<Expenses> result = new LinkedList<>();
        Util.getListMapFromEntity(entity).forEach(
                t -> {
                    int expensesType = (Integer) ((Map) t.get(EXPENSES_TYPE)).get("expensesTypeId");
                    t.put("type", getTypeDescriptionById(expensesType));
                    Expenses expenses = DataConverter.convertToExpenses(t);
                    result.add(expenses);
                }
        );
        return result;
    }

    public List<Map<String, Object>> getExpensesType() throws AuthenticationException, IOException {
        if (expensesTypeList != null) {
            return expensesTypeList;
        } else {
            HttpGet httpGet = new HttpGet("http://localhost:7004/budget/v2/expenses/types?user_id=" + USER_ID);
            authRequest(httpGet);
            HttpResponse response = client.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
                expensesTypeList = Util.getListMapFromEntity(response.getEntity());
                return expensesTypeList;
            }
        }
        return ImmutableList.of();
    }

    private String getTypeDescriptionById(Integer id) {
        try {
            return (String) getExpensesType().stream().filter(t -> t.get("id").equals(id)).findAny().orElse(ImmutableMap.of()).get("desc");
        } catch (AuthenticationException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Expenses saveExpenses(
            String month,
            String year,
            String type,
            String desc,
            String amountString,
            boolean testMode
    ) throws AuthenticationException, IOException {

        double amount = Util.parseAmountString(amountString);
        String typeDesc = getTypeDescriptionById(Integer.valueOf(type));
        Expenses result = Expenses.of(
                null,
                Integer.valueOf(month),
                Integer.valueOf(year),
                typeDesc,
                desc,
                LocalDate.now().toString(),
                amount
        );

        if (testMode) {
            return result;
        }

        String request = String.format(
                "http://localhost:7004/budget/v2/expenses/save?user_id=%d&month=%s&year=%s&type=%s&description=%s&amount=%s",
                USER_ID, month, year, type, desc.replace(" ", "+"), amount
        );

        HttpGet httpGet = new HttpGet(request);
        authRequest(httpGet);
        HttpResponse response = client.execute(httpGet);

        if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
            return result;
        }
        throw new RuntimeException("Http status not Ok" + EntityUtils.toString(response.getEntity()));
    }

    public String updateExpensesBalance(String stringAmount, boolean testMode) throws AuthenticationException, IOException {

        double amount = Util.parseAmountString(stringAmount);
        if (testMode) {
            return "" + amount;
        }

        HttpGet httpGet = new HttpGet(
                "http://localhost:7004/budget/v2/expenses/balance/update?amount=" + amount + "&user_id=" + USER_ID
        );
        authRequest(httpGet);
        HttpResponse response = client.execute(httpGet);

        if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
            return "" + amount;
        }
        throw new RuntimeException("Http status not Ok" + EntityUtils.toString(response.getEntity()));

    }

    public Map<String, String> fundUpdate(
            String stringAmount,
            String stringCurrency,
            String stringPrice,
            String description,
            boolean testMode
    ) throws AuthenticationException, IOException {

        double amount = Util.parseAmountString(stringAmount);
        double price = Double.parseDouble(stringPrice);
        int currency = Integer.valueOf(stringCurrency);
        String currencyValue = "Рубль";
        if (currency == USER_ID) currencyValue = "Доллар";
        if (currency == 2) currencyValue = "Евро";

        Map<String, String> result = ImmutableMap.of(
                "amount", String.valueOf(amount),
                "price", stringPrice,
                "currency", currencyValue,
                "description", description
        );

        if (testMode) {
            return result;
        }

        HttpGet httpGet = new HttpGet(
                String.format(
                        "http://localhost:7004/budget/v2/fund/save?user_id=%d&currency=%d&description=%s&price=%.2f&amount=%.2f",
                        USER_ID, currency, description.replace(" ", "+"), price, amount
                )
        );
        authRequest(httpGet);
        HttpResponse response = client.execute(httpGet);

        if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
            return result;
        }
        throw new RuntimeException("Http status not Ok" + EntityUtils.toString(response.getEntity()));
    }

    public Map<String, String> currencyUpdate(
            String stringCurrency,
            String stringPrice,
            boolean testMode
    ) throws AuthenticationException, IOException {
        double price = Double.parseDouble(stringPrice);
        int currency = Integer.valueOf(stringCurrency);
        String currencyValue = "Доллар";
        if (currency == 2) currencyValue = "Евро";

        Map<String, String> result = ImmutableMap.of(
                "price", stringPrice,
                "currency", currencyValue
        );

        if (testMode) {
            return result;
        }

        HttpGet httpGet = new HttpGet(
                String.format(
                        "http://localhost:7004/budget/v2/currency/update?user_id=%d&currency=%d&amount=%.2f",
                        USER_ID, currency, price
                )
        );
        authRequest(httpGet);
        HttpResponse response = client.execute(httpGet);

        if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
            return result;
        }
        throw new RuntimeException("Http status not Ok" + EntityUtils.toString(response.getEntity()));

    }

    public Map<String, Object> getStatisticCurrentMonth() throws AuthenticationException, IOException {

        HttpGet getExpensesAmount = new HttpGet(
                "http://localhost:7004/budget/v2/statistic/current?user_id=" + USER_ID);
        authRequest(getExpensesAmount);
        HttpResponse responseTotal = client.execute(getExpensesAmount);

        Map<String, Object> result = Util.getJSONObjectFromEntity(responseTotal.getEntity()).toMap();
        return result;
    }

    private List<Map<String, Object>> getYearlyStatisticFromService(int year) throws AuthenticationException, IOException {
        HttpGet getExpensesAmount = new HttpGet(
                "http://localhost:7004/budget/v2/statistic/expenses/yearly?year=" + year + "&user_id=" + USER_ID
        );
        authRequest(getExpensesAmount);
        HttpResponse response = client.execute(getExpensesAmount);
        return Util.getListMapFromEntity(response.getEntity());
    }

    public List<List<Object>> getYearlyMonthlyStatistic(int year) throws IOException, AuthenticationException {
        List<Map<String, Object>> origin = getYearlyStatisticFromService(year);
        List<List<Object>> statistics = new LinkedList<>();
        int m = 0;
        while (m++ < 12) {
            double monthlyAmount = 0;
            List<Object> monthStatistic = new LinkedList<>();
            monthStatistic.add(getMonthById(m));
            for (Map<String, Object> type : getExpensesType()) {
                boolean isIncome = (Boolean) type.get("income");
                String typeDesc = (String) type.get("desc");
                double amount = getMonthlyTotalByType(typeDesc, m, origin);
                monthStatistic.add(getRoundedValue(amount));
                if (!isIncome) {
                    monthlyAmount += amount;
                }
            }
            monthStatistic.add(getRoundedValue(monthlyAmount));
            statistics.add(monthStatistic);
        }
        return statistics;
    }

    public List<Object> getYearlyStatistic(int year) throws IOException, AuthenticationException {
        List<Map<String, Object>> origin = getYearlyStatisticFromService(year);
        List<Object> yearlyStatistic = new LinkedList<>();
        double yearlyExpensesAmount = 0;
        for (Map<String, Object> type : getExpensesType()) {
            boolean isIncome = (Boolean) type.get("income");
            String typeDesc = (String) type.get("desc");
            double amount = getTotalByType(typeDesc, origin);
            yearlyStatistic.add(getRoundedValue(amount));
            if (!isIncome) {
                yearlyExpensesAmount += amount;
            }
        }
        yearlyStatistic.add(getRoundedValue(yearlyExpensesAmount));
        return yearlyStatistic;
    }

    private double getMonthlyTotalByType(String typeDesc, int monthId, List<Map<String, Object>> origin) {
        double result = 0;
        for (Map<String, Object> map : origin) {
            String month = Integer.toString(monthId);
            if (map.get("month_id").equals(month)) {
                if (map.get("description").equals(typeDesc)) {
                    String amountString = (String) map.get("amount");
                    double amount = Double.parseDouble(amountString);
                    result += amount;
                }
            }
        }
        return result;
    }

    private double getTotalByType(String typeDesc, List<Map<String, Object>> origin) {
        double result = 0;
        for (Map<String, Object> map : origin) {
            if (map.get("description").equals(typeDesc)) {
                String amountString = (String) map.get("amount");
                double amount = Double.parseDouble(amountString);
                result += amount;
            }
        }
        return result;
    }

    private String getRoundedValue(double origin) {
        return String.format("%.1f", origin);
    }

    private String getMonthById(int id) {
        if (id < USER_ID && id > 12) {
            throw new RuntimeException("Incorrect month");
        }
        String month = "";
        switch (id) {
            case USER_ID:
                month = "Январь";
                break;
            case 2:
                month = "Февраль";
                break;
            case 3:
                month = "Март";
                break;
            case 4:
                month = "Апрель";
                break;
            case 5:
                month = "Май";
                break;
            case 6:
                month = "Июнь";
                break;
            case 7:
                month = "Июль";
                break;
            case 8:
                month = "Август";
                break;
            case 9:
                month = "Сентябрь";
                break;
            case 10:
                month = "Октябрь";
                break;
            case 11:
                month = "Ноябрь";
                break;
            case 12:
                month = "Декабрь";
                break;

        }
        return month;
    }
}
