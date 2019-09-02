package br.com.juli0mendes.services;

import static br.com.juli0mendes.builders.FilmeBuilder.umFilme;
import static br.com.juli0mendes.builders.LocacaoBuilder.umaLocacao;
import static br.com.juli0mendes.builders.UsuarioBuilder.umUsuario;
import static br.com.juli0mendes.matchers.MatchersProprios.caiNumaSegunda;
import static br.com.juli0mendes.matchers.MatchersProprios.ehHoje;
import static br.com.juli0mendes.matchers.MatchersProprios.ehHojeComDiferencaDias;
import static br.com.juli0mendes.utils.DataUtils.isMesmaData;
import static br.com.juli0mendes.utils.DataUtils.obterData;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import br.com.juli0mendes.builders.FilmeBuilder;
import br.com.juli0mendes.builders.UsuarioBuilder;
import br.com.juli0mendes.domains.Filme;
import br.com.juli0mendes.domains.Locacao;
import br.com.juli0mendes.domains.Usuario;
import br.com.juli0mendes.exceptions.FilmeSemEstoqueException;
import br.com.juli0mendes.exceptions.LocadoraException;
import br.com.juli0mendes.repositories.LocacaoDAO;
import br.com.juli0mendes.runners.ParallelRunner;
import br.com.juli0mendes.services.EmailService;
import br.com.juli0mendes.services.LocacaoService;
import br.com.juli0mendes.services.SPCService;
import br.com.juli0mendes.utils.DataUtils;

/**
 * The Class LocacaoServiceTest.
 *
 * @author Julio Cesar Mendes
 * 
 *         The Class LocacaoServiceTest.
 */
@RunWith(ParallelRunner.class)
public class LocacaoServiceTest {

	/** The locacao service. */
	@InjectMocks @Spy
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
		
		System.out.println("Iniciando 2...");
		
		filmes = Arrays.asList(umFilme().comValor(5.0).build());

		filmesSemEstoque = Arrays.asList(FilmeBuilder.umFilmeSemEstoque().build());
	}
	
	@After
	public void tearDown() {
		System.out.println("Finalizando 2...");
	}

	/**
	 * Deve alugar filme.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void deveAlugarFilme() throws Exception {

//		Assume.assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));

		// cenario
		Usuario usuario = UsuarioBuilder.umUsuario().build();
		
		Mockito.doReturn(DataUtils.obterData(28, 4, 2017)).when(this.locacaoService).obterData();
		
		// acao
		Locacao locacao = locacaoService.alugarFilme(usuario, filmes);

		// verificação com rule
		error.checkThat(locacao.getValor(), is(equalTo(5.0)));
		error.checkThat(locacao.getValor(), is(not(6.0)));
		error.checkThat(isMesmaData(locacao.getDataLocacao(), obterData(28, 4, 2017)), is(true));
		error.checkThat(isMesmaData(locacao.getDataRetorno(), obterData(29, 4, 2017)), is(true));
	}

	/**
	 * Nao deve alugar filme sem estoque.
	 *
	 * @throws Exception the exception
	 */
	// forma elegante
	@Test(expected = FilmeSemEstoqueException.class)
	public void naoDeveAlugarFilmeSemEstoque() throws Exception {
		// cenario
//		LocacaoService service = new LocacaoService();
		Usuario usuario = UsuarioBuilder.umUsuario().build();

		Mockito.when(spcService.possuiNegativacao(usuario)).thenReturn(false);

		// acao
		locacaoService.alugarFilme(usuario, filmesSemEstoque);
	}

	/**
	 * Nao deve alugar filme sem usuario.
	 *
	 * @throws FilmeSemEstoqueException the filme sem estoque exception
	 */
	// forma robusta
	@Test
	public void naoDeveAlugarFilmeSemUsuario() throws FilmeSemEstoqueException {
		// cenario
		LocacaoService locacaoService = new LocacaoService();

		// ação
		try {
			locacaoService.alugarFilme(null, filmes);
			fail();
		} catch (LocadoraException e) {
			assertThat(e.getMessage(), is("Usuário vazio"));
		}

//		System.out.println("Forma robusta");
	}

	/**
	 * Nao deve alugar filme sem filme.
	 *
	 * @throws FilmeSemEstoqueException the filme sem estoque exception
	 * @throws LocadoraException        the locadora exception
	 */
	// forma nova
	@Test
	public void naoDeveAlugarFilmeSemFilme() throws FilmeSemEstoqueException, LocadoraException {
		// cenario
		LocacaoService service = new LocacaoService();
		Usuario usuario = UsuarioBuilder.umUsuario().build();

		exception.expect(LocadoraException.class);
		exception.expectMessage("Filme vazio");

		// ação
		service.alugarFilme(usuario, null);

//		System.out.println("Forma nova");
	}

	/**
	 * Deve devolger na segunda ao alugar no sabado.
	 * @throws Exception 
	 */
	@Test
	public void deveDevolgerNaSegundaAoAlugarNoSabado() throws Exception {

//		Assume.assumeTrue(DataUtils.verificarDiaSemana(new Date(), Calendar.MONDAY));

		Usuario usuario = UsuarioBuilder.umUsuario().build();
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().build());
		
		Mockito.doReturn(DataUtils.obterData(29, 4, 2017)).when(this.locacaoService).obterData();

		Locacao retorno = locacaoService.alugarFilme(usuario, filmes);

		assertThat(retorno.getDataRetorno(), caiNumaSegunda());
	}

	/**
	 * Nao deve alugar filme para negativado spc.
	 * @throws Exception 
	 */
	@Test
	public void naoDeveAlugarFilmeParaNegativadoSpc() throws Exception {

		Usuario usuario = umUsuario().build();

		List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().build());

		when(spcService.possuiNegativacao(Mockito.any(Usuario.class))).thenReturn(true);

		try {
			locacaoService.alugarFilme(usuario, filmes);
			// verificacao
			fail();
		} catch (LocadoraException e) {
			assertThat(e.getMessage(), is("Usuário negativado"));
		}

		verify(spcService).possuiNegativacao(usuario);
	}

	/**
	 * Deve enviar email para locacoes atrasadas.
	 */
	@Test
	public void deveEnviarEmailParaLocacoesAtrasadas() {
		// cenario
		Usuario usuarioEmDia = umUsuario().comNome("Usuario em dia").build();
		Usuario usuarioAtrasado = umUsuario().build();
		Usuario outroUsarioAtrasado = umUsuario().comNome("Outro atrasado").build();

		List<Locacao> locacoesPendentes = Arrays.asList(umaLocacao().comUsuario(usuarioEmDia).build(),
				umaLocacao().atrasada().comUsuario(usuarioAtrasado).build(),
				umaLocacao().atrasada().comUsuario(outroUsarioAtrasado).build(),
				umaLocacao().atrasada().comUsuario(outroUsarioAtrasado).build());

		when(this.locacaoDAO.obterLocacoesPendentes()).thenReturn(locacoesPendentes);

		// acao
		this.locacaoService.notificarAtrasos();

		// verificação
		verify(emailService, times(3)).notificarAtraso(Mockito.any(Usuario.class));
		verify(emailService, never()).notificarAtraso(usuarioEmDia);
		verify(emailService).notificarAtraso(usuarioAtrasado);
		verify(emailService, atLeastOnce()).notificarAtraso(outroUsarioAtrasado);

		verifyNoMoreInteractions(emailService);
	}

	/**
	 * Deve tratar erro no SPC.
	 * @throws Exception 
	 */
	@Test
	public void deveTratarErroNoSPC() throws Exception {
		// cenario
		Usuario usuario = umUsuario().build();

		when(this.spcService.possuiNegativacao(usuario)).thenThrow(new Exception("Falha catastrófica"));

		// verificao
		exception.expect(LocadoraException.class);
		exception.expectMessage("Problemas no SPC, tente novamente");

		// acao
		this.locacaoService.alugarFilme(usuario, filmes);
	}
	
	/**
	 * Deve prorrogar uma locacao.
	 */
	@Test
	public void deveProrrogarUmaLocacao() {
		
		int qtdDias = 3;
		
		//cenario
		Locacao locacao = umaLocacao().build();
		
		//acap
		this.locacaoService.prorrogarLocacao(locacao, qtdDias);
		
		//verificacao
		ArgumentCaptor<Locacao> argumentCaptor = ArgumentCaptor.forClass(Locacao.class);
		
		Mockito.verify(this.locacaoDAO).salvar(argumentCaptor.capture());
		Locacao locacaoRetornada = argumentCaptor.getValue();
		
		this.error.checkThat(locacaoRetornada.getValor(), is(locacao.getValor() * qtdDias));
		this.error.checkThat(locacaoRetornada.getDataLocacao(), ehHoje());
		this.error.checkThat(locacaoRetornada.getDataRetorno(), ehHojeComDiferencaDias(qtdDias));
	}
	
	@Test
	public void deveCalcularValorLocacao() throws Exception {
		
		// cenario
		
		// acao
		Class<LocacaoService> clazz = LocacaoService.class;
		Method method = clazz.getDeclaredMethod("calcularValorLocacao", List.class);
		method.setAccessible(true);
		Double valor = (Double) method.invoke(this.locacaoService, filmes);
		
		// verificacao
		assertThat(valor, is(5.0));
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
//	public static void main(String[] args) {
//		new BuilderMaster().gerarCodigoClasse(Locacao.class);
//	}
}