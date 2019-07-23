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
import static org.mockito.Mockito.verifyZeroInteractions;
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
import org.mockito.Mockito;

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

public class LocacaoServiceTest {

	private LocacaoService locacaoService;
	private SPCService spcService;
	private EmailService emailService;

	private LocacaoDAO locacaoDAO;
	
	List<Filme> filmes = new ArrayList<Filme>();
	List<Filme> filmesSemEstoque = new ArrayList<Filme>();

	// definicao do contador
	@Rule
	public ErrorCollector error = new ErrorCollector();

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setUp() {
		locacaoService = new LocacaoService();
		
		filmes = Arrays.asList(br.ce.wcaquino.builders.FilmeBuilder.umFilme().comValor(5.0).build());
		
		filmesSemEstoque = Arrays.asList(FilmeBuilder.umFilmeSemEstoque().build());
		
		locacaoDAO = Mockito.mock(LocacaoDAO.class);
		locacaoService.setLocacaoDAO(locacaoDAO);
		
		spcService = Mockito.mock(SPCService.class);
		locacaoService.setSpcService(spcService);
		
		emailService = Mockito.mock(EmailService.class);
		locacaoService.setEmailService(emailService);
	}

	@Test
	public void deveAlugarFilme() throws Exception {
		
		Assume.assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
		
		// cenario
		Usuario usuario = UsuarioBuilder.umUsuario().build();

		// acao
		Locacao locacao = locacaoService.alugarFilme(usuario, filmes);

		// verificação com rule
		error.checkThat(locacao.getValor(), is(equalTo(5.0)));
		error.checkThat(locacao.getValor(), is(not(6.0)));
		error.checkThat(locacao.getDataLocacao(), ehHoje());
		error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterDataComDiferencaDias(1)), is(true));
		error.checkThat(locacao.getDataRetorno(), ehHojeComDiferencaDias(1));
	}

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
			assertThat(e.getMessage(), is("Usuário negativado"));
		}
		
		verify(spcService).possuiNegativacao(usuario);
	}
	
	@Test
	public void deveEnviarEmailParaLocacoesAtrasadas() {
		// cenario
		Usuario usuarioEmDia        = umUsuario().comNome("Usuario em dia").build();
		Usuario usuarioAtrasado     = umUsuario().build();
		Usuario outroUsarioAtrasado = umUsuario().comNome("Outro atrasado").build();
		
		List<Locacao> locacoesPendentes = Arrays.asList(
				umaLocacao().comUsuario(usuarioEmDia).build(),
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
	
	public static void main(String[] args) {
		new BuilderMaster().gerarCodigoClasse(Locacao.class);
	}
}