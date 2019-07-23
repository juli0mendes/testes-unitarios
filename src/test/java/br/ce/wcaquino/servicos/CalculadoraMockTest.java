package br.ce.wcaquino.servicos;

import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class CalculadoraMockTest {

	@Test
	public void teste() {

		Calculadora calculadora = Mockito.mock(Calculadora.class);
		
		ArgumentCaptor<Integer> argumentCaptor = ArgumentCaptor.forClass(Integer.class);
		
		when(calculadora.somar(argumentCaptor.capture(), argumentCaptor.capture())).thenReturn(5);
		
		Assert.assertEquals(5, calculadora.somar(1, 10000));
		
		System.out.println(argumentCaptor.getAllValues());
	}
}