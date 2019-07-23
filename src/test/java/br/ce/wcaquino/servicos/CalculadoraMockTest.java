package br.ce.wcaquino.servicos;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class CalculadoraMockTest {
	
	@Mock
	private Calculadora calcMock;
	
	@Spy
	private Calculadora calcSpy;
	
	@Mock
	private EmailService emailService;
	
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void deveMostrarADiferencaEntreMockESpy() {
		
		when(calcMock.somar(1, 2)).thenReturn(5);
//		when(calcSpy.somar(1, 2)).thenReturn(5);
		doReturn(5).when(calcSpy).somar(1, 2);
		
		doNothing().when(calcSpy).imprime();
		
		System.out.println("Mock: " + calcMock.somar(1, 2));
		System.out.println("Spy: " + calcSpy.somar(1, 2));
		
		System.out.println("Mock");
		calcMock.imprime();
		System.out.println("Spy");
		calcSpy.imprime();
	}

	@Test
	public void teste() {

		Calculadora calculadora = Mockito.mock(Calculadora.class);
		
		ArgumentCaptor<Integer> argumentCaptor = ArgumentCaptor.forClass(Integer.class);
		
		when(calculadora.somar(argumentCaptor.capture(), argumentCaptor.capture())).thenReturn(5);
		
		Assert.assertEquals(5, calculadora.somar(1, 10000));
		
		System.out.println(argumentCaptor.getAllValues());
	}
}