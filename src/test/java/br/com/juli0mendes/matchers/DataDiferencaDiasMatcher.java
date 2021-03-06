package br.com.juli0mendes.matchers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.internal.ReflectiveTypeFinder;

import br.com.juli0mendes.utils.DataUtils;

public class DataDiferencaDiasMatcher extends TypeSafeMatcher<Date> {
	
	private Integer qtdDias;
	
	public DataDiferencaDiasMatcher(Integer qtdDias) {
		this.qtdDias = qtdDias;
	}

	public DataDiferencaDiasMatcher(Class<?> expectedType) {
		super(expectedType);
	}

	public DataDiferencaDiasMatcher(ReflectiveTypeFinder typeFinder) {
		super(typeFinder);
	}

//	@Override
	public void describeTo(Description description) {
		Date dataEsperada = DataUtils.obterDataComDiferencaDias(qtdDias);
		
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY");
		
		description.appendText(dateFormat.format(dataEsperada));
	}

	@Override
	protected boolean matchesSafely(Date data) {
		return DataUtils.isMesmaData(data, DataUtils.obterDataComDiferencaDias(qtdDias));
	}

}
