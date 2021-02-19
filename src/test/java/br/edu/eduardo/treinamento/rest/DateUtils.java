package br.edu.eduardo.treinamento.rest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

	public static String obterDataDiferencaDias(int dias) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, dias);
		return obterDataFormatada(cal.getTime());
	}
	
	public static String obterDataFormatada(Date date) {
		DateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
		return formato.format(date);
	}
}
