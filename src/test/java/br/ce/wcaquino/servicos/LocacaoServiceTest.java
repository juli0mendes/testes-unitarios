package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.builders.FilmeBuilder.umFilme;
import static br.ce.wcaquino.builders.LocacaoBuilder.umaLocacao;
import static br.ce.wcaquino.builders.UsuarioBuilder.umUsuario;
import static br.ce.wcaquino.matchers.MatchersProprios.caiNumaSegunda;
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
import java.util.Date;
import java.util.List;

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
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.ce.wcaquino.builders.FilmeBuilder;
import br.ce.wcaquino.builders.UsuarioBuilder;
import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;

/**
 * The Class LocacaoServiceTest.
 *
 * @author Julio Cesar Mendes
 * 
 *         The Class LocacaoServiceTest.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({LocacaoService.class, DataUtils.class})
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

//		Assume.assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));

		// cenario
		Usuario usuario = UsuarioBuilder.umUsuario().build();
		
		PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(DataUtils.obterData(28, 4, 2017));

		// acao
		Locacao locacao = locacaoService.alugarFilme(usuario, filmes);

		// verificação com rule
		error.checkThat(locacao.getValor(), is(equalTo(5.0)));
		error.checkThat(locacao.getValor(), is(not(6.0)));
		error.checkThat(locacao.getDataLocacao(), ehHoje());
		error.checkThat(locacao.getDataRetorno(), ehHojeComDiferencaDias(1));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), DataUtils.obterData(28, 4, 2017)), is(true));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterData(29, 4, 2017)), is(true));
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
		
		PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(DataUtils.obterData(29, 4, 2017));

		Locacao retorno = locacaoService.alugarFilme(usuario, filmes);

//		assertThat(retorno.getDataRetorno(), new DiaSemanaMatcher(Calendar.MONDAY));
//		assertThat(retorno.getDataRetorno(), caiEm(Calendar.TUESDAY));
		assertThat(retorno.getDataRetorno(), caiNumaSegunda());
		
		PowerMockito.verifyNew(Date.class, Mockito.times(2)).withNoArguments();
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
	
	

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
//	public static void main(String[] args) {
//		new BuilderMaster().gerarCodigoClasse(Locacao.class);
//	}
}