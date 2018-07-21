package com.ui.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.ui.mvc.model.Expenses;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ExpensesService {

    private CloseableHttpClient client = HttpClients.createDefault();
    private String user;
    private String password;
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
                new HttpGet(String.format("http://localhost:7004/budget/expenses?month=%d&year=%d", now.getMonthValue(), now.getYear()));
        authRequest(httpGet);
        HttpResponse response = client.execute(httpGet);

        if (response.getStatusLine().getStatusCode()== HttpStatus.OK.value()) {
            return getExpensesByEntity(response.getEntity());
        }

        System.out.println(response.getStatusLine());
        return ImmutableList.of();
    }

    private List<Expenses> getExpensesByEntity (HttpEntity entity) throws IOException {
        List<Expenses> result = new LinkedList<>();
        Util.getListMapFromEntity(entity).forEach(
                t -> {
                    int typeId = (Integer) t.get("type");
                    t.put("type", getTypeDescriptionById(typeId));
                    Expenses expenses = DataConverter.convertToExpenses(t);
                    result.add(expenses);
                }
        );
        return result;
    }

    public List<Map<String, Object>> getExpensesType() throws AuthenticationException, IOException {
        if (expensesTypeList != null) {
            return  expensesTypeList;
        } else {
            HttpGet httpGet = new HttpGet("http://localhost:7004/budget/expenses/type");
            authRequest(httpGet);
            HttpResponse response = client.execute(httpGet);
            if (response.getStatusLine().getStatusCode()== HttpStatus.OK.value()) {
                expensesTypeList = Util.getListMapFromEntity(response.getEntity());
                return expensesTypeList;
            }
        }
        return ImmutableList.of();
    }

    public String getTypeDescriptionById(Integer id) {
        try {
            return (String) getExpensesType().stream().filter(t -> t.get("id").equals(id)).findAny().orElse(ImmutableMap.of()).get("desc");
        } catch (AuthenticationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
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
                "http://localhost:7004/budget/expenses/save?month=%s&year=%s&type=%s&description=%s&amount=%s",
                month, year, type, desc.replace(" ", "+"), amount
        );

        HttpGet httpGet = new HttpGet(request);
        authRequest(httpGet);
        HttpResponse response = client.execute(httpGet);

        if (response.getStatusLine().getStatusCode()==HttpStatus.OK.value()) {
            return result;
        }
        throw new RuntimeException("Http status not Ok" + EntityUtils.toString(response.getEntity()));
    }

    public String updateExpensesBalance(String stringAmount, boolean testMode) throws AuthenticationException, IOException {

        double amount = Util.parseAmountString(stringAmount);
        if (testMode) {
            return "" + amount;
        }

        HttpGet httpGet = new HttpGet("http://localhost:7004/budget/expenses/update/expenses_balance?amount="+amount);
        authRequest(httpGet);
        HttpResponse response = client.execute(httpGet);

        if (response.getStatusLine().getStatusCode()==HttpStatus.OK.value()) {
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
        if (currency == 1) currencyValue = "Доллар";
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
                        "http://localhost:7004/budget/fund/save?currency=%d&description=%s&price=%.2f&amount=%.2f",
                        currency, description.replace(" ", "+"), price, amount
                )
        );
        authRequest(httpGet);
        HttpResponse response = client.execute(httpGet);

        if (response.getStatusLine().getStatusCode()==HttpStatus.OK.value()) {
            return result;
        }
        throw new RuntimeException("Http status not Ok" + EntityUtils.toString(response.getEntity()));

    }



    private void authRequest(HttpRequest request) throws AuthenticationException {
        UsernamePasswordCredentials basicAuth = new UsernamePasswordCredentials(user, password);
        request.addHeader(new BasicScheme().authenticate(basicAuth, request, null));
    }

    public Map<String, Object> getStatisticCurrentMonth() throws AuthenticationException, IOException {

        HttpGet getExpensesAmount = new HttpGet("http://localhost:7004/budget/statistic/current");
        authRequest(getExpensesAmount);
        HttpResponse responseTotal = client.execute(getExpensesAmount);

        Map<String, Object> result =Util.getJSONObjectFromEntity(responseTotal.getEntity()).toMap();
        return result;
    }
}
