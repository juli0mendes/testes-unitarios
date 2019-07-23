package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.builders.LocacaoBuilder.umaLocacao;
import static br.ce.wcaquino.builders.UsuarioBuilder.umUsuario;
import static br.ce.wcaquino.matchers.MatchersProprios.caiEm;
import static br.ce.wcaquino.matchers.MatchersProprios.ehHoje;
import static br.ce.wcaquino.matchers.MatchersProprios.ehHojeComDiferencaDias;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import br.ce.wcaquino.builders.FilmeBuilder;
import br.ce.wcaquino.builders.UsuarioBuilder;
import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;
import buildermaster.BuilderMaster;

/**
 * @author Julio Cesar Mendes
 * 
 *         The Class LocacaoServiceTest.
 */
public class LocacaoServiceTest {

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

		filmes = Arrays.asList(br.ce.wcaquino.builders.FilmeBuilder.umFilme().comValor(5.0).build());

		filmesSemEstoque = Arrays.asList(FilmeBuilder.umFilmeSemEstoque().build());
	}

	/**
	 * Deve alugar filme.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void deveAlugarFilme() throws Exception {

		Assume.assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));

		// cenario
		Usuario usuario = UsuarioBuilder.umUsuario().build();

		// acao
		Locacao locacao = locacaoService.alugarFilme(usuario, filmes);

		// verifica��o com rule
		error.checkThat(locacao.getValor(), is(equalTo(5.0)));
		error.checkThat(locacao.getValor(), is(not(6.0)));
		error.checkThat(locacao.getDataLocacao(), ehHoje());
		error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterDataComDiferencaDias(1)),
				is(true));
		error.checkThat(locacao.getDataRetorno(), ehHojeComDiferencaDias(1));
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

		// a��o
		try {
			locacaoService.alugarFilme(null, filmes);
			fail();
		} catch (LocadoraException e) {
			assertThat(e.getMessage(), is("Usu�rio vazio"));
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

		// a��o
		service.alugarFilme(usuario, null);

//		System.out.println("Forma nova");
	}

	/**
	 * Deve devolger na segunda ao alugar no sabado.
	 *
	 * @throws FilmeSemEstoqueException the filme sem estoque exception
	 * @throws LocadoraException        the locadora exception
	 */
	@Test
	public void deveDevolgerNaSegundaAoAlugarNoSabado() throws FilmeSemEstoqueException, LocadoraException {

		Assume.assumeTrue(DataUtils.verificarDiaSemana(new Date(), Calendar.MONDAY));

		Usuario usuario = UsuarioBuilder.umUsuario().build();
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().build());

		Locacao retorno = locacaoService.alugarFilme(usuario, filmes);

//		assertThat(retorno.getDataRetorno(), new DiaSemanaMatcher(Calendar.MONDAY));
		assertThat(retorno.getDataRetorno(), caiEm(Calendar.TUESDAY));
//		assertThat(retorno.getDataRetorno(), caiNumaSegunda());
	}

	/**
	 * Nao deve alugar filme para negativado spc.
	 *
	 * @throws FilmeSemEstoqueException the filme sem estoque exception
	 */
	@Test
	public void naoDeveAlugarFilmeParaNegativadoSpc() throws FilmeSemEstoqueException {

		Usuario usuario = umUsuario().build();

		List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().build());

		when(spcService.possuiNegativacao(Mockito.any(Usuario.class))).thenReturn(true);

		try {
			locacaoService.alugarFilme(usuario, filmes);
			// verificacao
			fail();
		} catch (LocadoraException e) {
			assertThat(e.getMessage(), is("Usu�rio negativado"));
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

		// verifica��o
		verify(emailService, times(3)).notificarAtraso(Mockito.any(Usuario.class));
		verify(emailService, never()).notificarAtraso(usuarioEmDia);
		verify(emailService).notificarAtraso(usuarioAtrasado);
		verify(emailService, atLeastOnce()).notificarAtraso(outroUsarioAtrasado);

		verifyNoMoreInteractions(emailService);
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		new BuilderMaster().gerarCodigoClasse(Locacao.class);
	}
}