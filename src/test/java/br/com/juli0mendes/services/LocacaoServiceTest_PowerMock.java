package br.com.juli0mendes.services;

import static br.com.juli0mendes.builders.FilmeBuilder.umFilme;
import static br.com.juli0mendes.matchers.MatchersProprios.caiNumaSegunda;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import br.com.juli0mendes.builders.FilmeBuilder;
import br.com.juli0mendes.builders.UsuarioBuilder;
import br.com.juli0mendes.domains.Filme;
import br.com.juli0mendes.domains.Locacao;
import br.com.juli0mendes.domains.Usuario;
import br.com.juli0mendes.repositories.LocacaoDAO;
import br.com.juli0mendes.utils.DataUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LocacaoService.class})
public class LocacaoServiceTest_PowerMock {

	@InjectMocks
	private LocacaoService locacaoService;

	@Mock
	private SPCService spcService;

	@Mock
	private EmailService emailService;

	@Mock
	private LocacaoDAO locacaoDAO;

	private List<Filme> filmes = new ArrayList<Filme>();

	List<Filme> filmesSemEstoque = new ArrayList<Filme>();

	// definicao do contador
	@Rule
	public ErrorCollector error = new ErrorCollector();

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setUp() {

		MockitoAnnotations.initMocks(this);
		
		locacaoService = PowerMockito.spy(locacaoService);

		filmes = Arrays.asList(umFilme().comValor(5.0).build());

		filmesSemEstoque = Arrays.asList(FilmeBuilder.umFilmeSemEstoque().build());
		
		System.out.println("Iniciando 4...");
		CalculadoraTest.ordem.append(4);
	}
	
	@After
	public void tearDown() {
		System.out.println("Finalizando 4...");
	}
	
	@AfterClass
	public static void tearDownClass() {
		System.out.println(CalculadoraTest.ordem.toString());
	}

	@Test
	public void deveAlugarFilme() throws Exception {

		// cenario
		Usuario usuario = UsuarioBuilder.umUsuario().build();
		
//		Calendar calendar = Calendar.getInstance();
//		calendar.set(Calendar.DAY_OF_MONTH, 28);
//		calendar.set(Calendar.MONTH, Calendar.APRIL);
//		calendar.set(Calendar.YEAR, 2017);
		
		PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(DataUtils.obterData(28, 4, 2017));
		
//		PowerMockito.mockStatic(Calendar.class);
//		PowerMockito.when(Calendar.getInstance()).thenReturn(calendar);
		
		// acao
		Locacao locacao = locacaoService.alugarFilme(usuario, filmes);

		// verificação com rule
		error.checkThat(locacao.getValor(), is(equalTo(5.0)));
		error.checkThat(locacao.getValor(), is(not(6.0)));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), DataUtils.obterData(28, 4, 2017)), is(true));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterData(29, 4, 2017)), is(true));
	}

	/**
	 * Deve devolger na segunda ao alugar no sabado.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void deveDevolgerNaSegundaAoAlugarNoSabado() throws Exception {

//		Assume.assumeTrue(DataUtils.verificarDiaSemana(new Date(), Calendar.MONDAY));

		Usuario usuario = UsuarioBuilder.umUsuario().build();
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().build());
		
		PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(DataUtils.obterData(29, 4, 2017));
		
//		Calendar calendar = Calendar.getInstance();
//		calendar.set(Calendar.DAY_OF_MONTH, 29);
//		calendar.set(Calendar.MONTH, Calendar.APRIL);
//		calendar.set(Calendar.YEAR, 2017);
//		
//		PowerMockito.mockStatic(Calendar.class);
//		PowerMockito.when(Calendar.getInstance()).thenReturn(calendar);

		Locacao retorno = locacaoService.alugarFilme(usuario, filmes);

		assertThat(retorno.getDataRetorno(), caiNumaSegunda());
		
//		PowerMockito.verifyStatic(Mockito.times(2));
//		Calendar.getInstance();
	}
	
	/**
	 * Deve alugar filme sem calcular valor.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void deveAlugarFilmeSemCalcularValor() throws Exception {
		
		// cenario
		Usuario usuario = UsuarioBuilder.umUsuario().build();
		
		PowerMockito.doReturn(1.0).when(locacaoService, "calcularValorLocacao", filmes);
		
		// acao
		Locacao locacao = locacaoService.alugarFilme(usuario, filmes);
		
		// verificacao
		assertThat(locacao.getValor(), is(1.0));
		
		PowerMockito.verifyPrivate(this.locacaoService).invoke("calcularValorLocacao", filmes);
	}
	
	/**
	 * Deve calcular valor locacao.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void deveCalcularValorLocacao() throws Exception {
		
		// acao
		Double valor = (Double) Whitebox.invokeMethod(this.locacaoService, "calcularValorLocacao", filmes);
		
		// verificacao
		assertThat(valor, is(5.0));
	}
}