package br.com.juli0mendes.services;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import br.com.juli0mendes.exceptions.NaoPodeDividirPorZeroException;

public class CalculadoraTest {
	
	public static StringBuffer ordem = new StringBuffer();
	
	public Calculadora calculadora;
	
	@Before
	public void setUp() {
		calculadora = new Calculadora();
		System.out.println("Iniciando...");
		ordem.append(1);
	}
	
	@After
	public void tearDown() {
		System.out.println("Finalizando...");
	}
	
	@AfterClass
	public static void tearDownClass() {
		System.out.println(ordem.toString());
	}

	@Test
	public void deveSomarDoisValores() {
		// cenario
		int a = 5;
		int b = 3;

		// acao
		int resultado = calculadora.somar(a, b);

		// verificacao
		assertEquals(8, resultado);
	}

	@Test
	public void deveSubtrairDoisValores() {
		// cenario
		int a = 10;
		int b = 4;

		// acao
		int resultado = calculadora.subtrair(a, b);

		// verificacao
		assertEquals(6, resultado);
	}

	@Test
	public void deveDividirDoisValores() throws NaoPodeDividirPorZeroException {
		// cenario
		int a = 6;
		int b = 3;

		// acao
		int resultado = calculadora.dividir(a, b);

		// verificacao
		assertEquals(2, resultado);
	}

	@Test(expected = NaoPodeDividirPorZeroException.class)
	public void deveLancarExcecaoAoDividirPorZero() throws NaoPodeDividirPorZeroException {
		// cenario
		int a = 10;
		int b = 0;

		// acao
		int resultado = calculadora.dividir(a, b);

		// verificacao
		assertEquals(2, resultado);
	}
	
	@Test
	public void deveDividir() {
		String a = "6";
		String b = "3";
		
		int resultado = calculadora.divide(a, b);
		
		assertEquals(2, resultado);
	}
}
