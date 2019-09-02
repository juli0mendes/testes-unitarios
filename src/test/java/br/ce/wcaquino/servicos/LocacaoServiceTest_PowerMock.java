package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.builders.FilmeBuilder.umFilme;
import static br.ce.wcaquino.matchers.MatchersProprios.caiNumaSegunda;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import br.ce.wcaquino.builders.FilmeBuilder;
import br.ce.wcaquino.builders.UsuarioBuilder;
import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.utils.DataUtils;

/**
 * The Class LocacaoServiceTest_PowerMock.
 *
 * @author Julio Cesar Mendes
 * 
 *         The Class LocacaoServiceTest.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({LocacaoService.class})
public class LocacaoServiceTest_PowerMock {

	/** The locacao service. */
	@InjectMocks
	private LocacaoService locacaoService;

	/** The spc service. */
	@Mock
	private SPCService spcService;

	/** The email service. */
	@Mock
	private EmailService emailService;

	/** The locacao DAO. */
	@Mock
	private LocacaoDAO locacaoDAO;

	/** The filmes. */
	List<Filme> filmes = new ArrayList<Filme>();

	/** The filmes sem estoque. */
	List<Filme> filmesSemEstoque = new ArrayList<Filme>();

	/** The error. */
	// definicao do contador
	@Rule
	public ErrorCollector error = new ErrorCollector();

	/** The exception. */
	@Rule
	public ExpectedException exception = ExpectedException.none();

	/**
	 * Sets the up.
	 */
	@Before
	public void setUp() {

		MockitoAnnotations.initMocks(this);
		
		locacaoService = PowerMockito.spy(locacaoService);

		filmes = Arrays.asList(umFilme().comValor(5.0).build());

		filmesSemEstoque = Arrays.asList(FilmeBuilder.umFilmeSemEstoque().build());
	}

	/**
	 * Deve alugar filme.
	 *
	 * @throws Exception the exception
	 */
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