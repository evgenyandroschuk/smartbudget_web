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

package com.ui.mvc;

import com.google.common.collect.ImmutableMap;
import com.ui.mvc.model.Expenses;
import com.ui.service.ExpensesService;
import com.ui.service.Util;
import org.apache.http.auth.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Eugene Androschuk
 */
@Controller
@RequestMapping("/")
public class BudgetController {

	private static final String USER = "user1";
	private static final String PASSWORD = "password1";

	@RequestMapping(method = RequestMethod.GET)
	public String index() {
		return "index";
	}

	@RequestMapping(value = "/expenses", method = RequestMethod.GET)
	public String expenses(Model model) throws IOException, AuthenticationException {

		ExpensesService expensesService = ExpensesService.of(USER, PASSWORD);
		List<Expenses> expensesList = expensesService.getCurrentMonthExpenses();

		Map<String, Double> totalList = new HashMap<>();
		expensesList.forEach(t -> {
			double sum = totalList.get(t.getType()) == null ?  0 : totalList.get(t.getType());
			totalList.put(t.getType(), t.getAmount() + sum );
		});

		model.addAttribute("results", expensesList);
		model.addAttribute("totals", totalList.entrySet());
		model.addAttribute("statistic", expensesService.getStatisticCurrentMonth());
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

	@RequestMapping(value = "/vehicle", method = RequestMethod.GET)
	public String vehicle() {
		return "vehicle/vehicle";
	}

	@RequestMapping(value = "/communication", method = RequestMethod.GET)
	public String communication() {
		return "communication/communication";
	}

	@RequestMapping("foo")
	public String foo() {
		throw new RuntimeException("Expected exception in controller");
	}

}
