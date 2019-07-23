package br.ce.wcaquino.servicos;

import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;

public class CalculadoraMockTest {

	@Test
	public void teste() {

		Calculadora calculadora = Mockito.mock(Calculadora.class);
		
		when(calculadora.somar(Mockito.eq(1), Mockito.anyInt())).thenReturn(5);
		
		System.out.println(calculadora.somar(1, 8));
	}
}