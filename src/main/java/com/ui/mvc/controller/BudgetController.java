/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.ui.mvc.controller;

import com.google.common.collect.ImmutableMap;
import com.ui.mvc.model.Expenses;
import com.ui.service.ExpensesService;
import org.apache.http.auth.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Eugene Androschuk
 */
@Controller
public class BudgetController extends AbstractController {

	@RequestMapping(method = RequestMethod.GET)
	public String index() {
		return "index";
	}

	@RequestMapping(value = "/expenses", method = RequestMethod.GET)
	public String expenses(Model model) {

		ExpensesService expensesService = ExpensesService.of(USER, PASSWORD);
		List<Expenses> expensesList = null;
		Map<String, Object> statisticCurrentMonth = null;
		try {
			expensesList = expensesService.getCurrentMonthExpenses();
			statisticCurrentMonth = expensesService.getStatisticCurrentMonth();
		} catch (AuthenticationException e) {
			model.addAttribute("message", "Проблема с аутентификацей к сервису с данными " );
			return "common_error";
		} catch (IOException e) {
			model.addAttribute("message", "Недоступен сервер с данными.");
			return "common_error";
		}

		Map<String, Double> totalList = new HashMap<>();
		expensesList.forEach(t -> {
			double sum = totalList.get(t.getType()) == null ?  0 : totalList.get(t.getType());
			totalList.put(t.getType(), t.getAmount() + sum );
		});

		model.addAttribute("results", expensesList);
		model.addAttribute("totals", totalList.entrySet());
		model.addAttribute("statistic", statisticCurrentMonth);
		return "expenses/expenses";
	}

	@RequestMapping(value = "/expenses/form", method = RequestMethod.GET)
	public String expensesForm(Model model) throws IOException, AuthenticationException {
		ExpensesService expensesService = ExpensesService.of(USER, PASSWORD);

		Map<String,String> months = new ImmutableMap.Builder<String,String>()
				.put("1", "Январь")
				.put("2", "Февраль")
				.put("3", "Март")
				.put("4", "Апрель")
				.put("5", "Май")
				.put("6", "Июнь")
				.put("7", "Июль")
				.put("8", "Август")
				.put("9", "Сентябрь")
				.put("10", "Октябрь")
				.put("11", "Ноябрь")
				.put("12", "Декабрь")
				.build();

		model.addAttribute("types", expensesService.getExpensesType());
		model.addAttribute("months", months.entrySet());
		model.addAttribute("current_month", ""+LocalDate.now().getMonthValue());
		model.addAttribute("year", "" + LocalDate.now().getYear());
		return "expenses/expenses_add";
	}

	@RequestMapping(value = "/expenses/response/add", method = RequestMethod.GET)
	public String expensesResponse(Model model,
			@RequestParam(value="month") String month,
			@RequestParam(value="year") String year,
			@RequestParam(value="type") String type,
			@RequestParam(value="desc") String desc,
			@RequestParam(value = "amount") String amount
	) throws IOException, AuthenticationException {

		Expenses expenses = ExpensesService.of(USER, PASSWORD).saveExpenses(month, year, type, desc, amount, false);
		model.addAttribute("expenses", expenses);
		return "expenses/expenses_add_response";
	}

	@RequestMapping(value = "/expenses/balance/update", method = RequestMethod.GET)
	public String updateExpensesBalance()  {
		return "expenses/expenses_update_balance";
	}

	@RequestMapping(value = "/expenses/balance/response", method = RequestMethod.GET)
	public String updateExpensesBalanceResponse(Model model,
			@RequestParam(value = "amount") String amount
	) throws IOException, AuthenticationException {

		String updatedAmount = ExpensesService.of(USER, PASSWORD).updateExpensesBalance(amount, false);
		model.addAttribute("updated_amount", updatedAmount);
		return "expenses/expenses_update_balance_response";
	}

	@RequestMapping(value = "expenses/fund/update", method = RequestMethod.GET)
	public String fundUpdate(Model model) {

		Map<String,String> currencies = new ImmutableMap.Builder<String,String>()
				.put("1", "Доллар")
				.put("2", "Евро")
				.put("3", "Рубль")
				.build();

		model.addAttribute("currencies", currencies.entrySet());
		return "expenses/fund_update";
	}

	@RequestMapping(value = "expenses/fund/response", method = RequestMethod.GET)
	public String fundResponse(Model model,
			@RequestParam(value = "amount") String amount,
			@RequestParam(value = "currency") String currency,
			@RequestParam(value = "price") String price,
			@RequestParam(value = "description") String description
	) throws IOException, AuthenticationException {

		Map<String, String> result =
				ExpensesService.of(USER, PASSWORD).fundUpdate(amount, currency, price, description, false);

		model.addAttribute("result", result);
		return "expenses/fund_response";
	}

	@RequestMapping(value = "expenses/currency/update", method = RequestMethod.GET)
	public String currencyUpdate(Model model) {

		Map<String,String> currencies = new ImmutableMap.Builder<String,String>()
				.put("1", "Доллар")
				.put("2", "Евро")
				.build();

		model.addAttribute("currencies", currencies.entrySet());
		return "expenses/currency_update";
	}

	@RequestMapping(value = "expenses/currency/response", method = RequestMethod.GET)
	public String currencyResponse(Model model,
							   @RequestParam(value = "currency") String currency,
							   @RequestParam(value = "price") String price
	) throws IOException, AuthenticationException {

		Map<String, String> result =
				ExpensesService.of(USER, PASSWORD).currencyUpdate(currency, price, false);

		model.addAttribute("result", result);
		return "expenses/currency_response";
	}

	/*  ======Reports Block=============  */

	@RequestMapping(value = "expenses/reports", method = RequestMethod.GET)
	public String reports(){
		return "expenses/reports/reports_main";
	}

	@RequestMapping(value = "expenses/reports/yearly", method = RequestMethod.GET)
	public String yearlyReport(Model model, @RequestParam(value = "year") Integer year) throws IOException, AuthenticationException {
		ExpensesService expensesService = ExpensesService.of(USER, PASSWORD);
		model.addAttribute("year", year);
		model.addAttribute("types", expensesService.getExpensesType());
		model.addAttribute("statistic", expensesService.getYearlyMonthlyStatistic(year));
		model.addAttribute("yearlyStatistic", expensesService.getYearlyStatistic(year));
		return "expenses/reports/yearly_response";
	}

	@RequestMapping(value = "expenses/reports/description", method = RequestMethod.GET)
	public String reportByDescription(
			Model model,
			@RequestParam String description,
			@RequestParam String start,
			@RequestParam String end
	) throws IOException, AuthenticationException {
		ExpensesService expensesService = ExpensesService.of(USER, PASSWORD);
		List<Expenses> expensesList = expensesService.getExpensesByDescription(description, start, end);

		String startDate = LocalDate.parse(start, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
				.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
		String endDate = LocalDate.parse(end, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
				.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

		double totalAmount = expensesList.stream().collect(Collectors.summarizingDouble(t -> t.getAmount())).getSum();
		model.addAttribute("results", expensesList);
		model.addAttribute("total", totalAmount);
		model.addAttribute("start", startDate);
		model.addAttribute("end", endDate);
		return "expenses/reports/reports_description";
	}

	@RequestMapping(value = "expenses/reports/month", method = RequestMethod.GET)
	public String reportByMonth(
			Model model,
			@RequestParam Integer year,
			@RequestParam Integer month
	) throws IOException, AuthenticationException {
		ExpensesService expensesService = ExpensesService.of(USER, PASSWORD);
		List<Expenses> expensesList = expensesService.getExpensesByYearMonth(year, month);

		Map<String, Double> totalList = new HashMap<>();
		expensesList.forEach(t -> {
			double sum = totalList.get(t.getType()) == null ?  0 : totalList.get(t.getType());
			totalList.put(t.getType(), t.getAmount() + sum );
		});

		double totalAmount = expensesList.stream()
				.filter(t -> !t.getType().equals("Income"))
				.collect(Collectors.summarizingDouble(t -> t.getAmount()))
				.getSum();
		model.addAttribute("results", expensesList);
		model.addAttribute("total", totalAmount);
		model.addAttribute("month", month);
		model.addAttribute("year", year);
		model.addAttribute("totals", totalList);
		return "expenses/reports/reports_month";
	}


	@RequestMapping("foo")
	public String foo() {
		throw new RuntimeException("Expected exception in controller");
	}

}
