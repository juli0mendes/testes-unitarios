package br.com.juli0mendes.suites;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import br.com.juli0mendes.services.CalculadoraTest;
import br.com.juli0mendes.services.CalculoValorLocacaoTest;
import br.com.juli0mendes.services.LocacaoServiceTest;

//@RunWith(Suite.class)
@SuiteClasses({
//	CalculadoraTest.class,
	CalculoValorLocacaoTest.class,
	LocacaoServiceTest.class
})
public class SuiteExecucao {
//	
//	@AfterClass
//	public static void after() {
//		System.out.println("after");
//	}
//	
//	@BeforeClass
//	public static void before() {
//		System.out.println("before");
//	}
}