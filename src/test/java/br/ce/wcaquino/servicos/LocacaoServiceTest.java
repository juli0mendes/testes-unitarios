package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.matchers.MatchersProprios.caiEm;
import static br.ce.wcaquino.matchers.MatchersProprios.ehHoje;
import static br.ce.wcaquino.matchers.MatchersProprios.ehHojeComDiferencaDias;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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
import br.ce.wcaquino.builders.LocacaoBuilder;
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

	private LocacaoService service;
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
		service = new LocacaoService();
		
		filmes = Arrays.asList(br.ce.wcaquino.builders.FilmeBuilder.umFilme().comValor(5.0).build());
		
		filmesSemEstoque = Arrays.asList(FilmeBuilder.umFilmeSemEstoque().build());
		
		locacaoDAO = Mockito.mock(LocacaoDAO.class);
		service.setLocacaoDAO(locacaoDAO);
		
		spcService = Mockito.mock(SPCService.class);
		service.setSpcService(spcService);
		
		emailService = Mockito.mock(EmailService.class);
		service.setEmailService(emailService);
	}

	@Test
	public void deveAlugarFilme() throws Exception {
		
		Assume.assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
		
		// cenario
		Usuario usuario = UsuarioBuilder.umUsuario().build();

		// acao
		Locacao locacao = service.alugarFilme(usuario, filmes);

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
		service.alugarFilme(usuario, filmesSemEstoque);
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
		
		Locacao retorno = service.alugarFilme(usuario, filmes);
		
//		assertThat(retorno.getDataRetorno(), new DiaSemanaMatcher(Calendar.MONDAY));
		assertThat(retorno.getDataRetorno(), caiEm(Calendar.TUESDAY));
//		assertThat(retorno.getDataRetorno(), caiNumaSegunda());
	}
	
	@Test
	public void naoDeveAlugarFilmeParaNegativadoSpc() throws FilmeSemEstoqueException {
		Usuario usuario = UsuarioBuilder.umUsuario().build();

		List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().build());
		
		Mockito.when(spcService.possuiNegativacao(Mockito.any(Usuario.class))).thenReturn(true);
		
		try {
			service.alugarFilme(usuario, filmes);
			// verificacao
			fail();
		} catch (LocadoraException e) {
			assertThat(e.getMessage(), is("Usuário negativado"));
		}
		Mockito.verify(spcService).possuiNegativacao(usuario);
	}
	
	@Test
	public void deveEnviarEmailParaLocacoesAtrasadas() {
		// cenario
		Usuario usuario = UsuarioBuilder.umUsuario().build();
		Usuario usuario2 = UsuarioBuilder.umUsuario().comNome("Usuario em dia").build();
		Usuario usuario3 = UsuarioBuilder.umUsuario().comNome("Outro atrasado").build();
		
		List<Locacao> locacoesPendentes = Arrays.asList(
				LocacaoBuilder.umaLocacao().atrasada().comUsuario(usuario).build(),
				LocacaoBuilder.umaLocacao().comUsuario(usuario2).build(),
				LocacaoBuilder.umaLocacao().atrasada().comUsuario(usuario3).build(),
				LocacaoBuilder.umaLocacao().atrasada().comUsuario(usuario3).build());
		Mockito.when(this.locacaoDAO.obterLocacoesPendentes()).thenReturn(locacoesPendentes);
		
		// acao
		service.notificarAtrasos();
		
		// verificação
		Mockito.verify(emailService, Mockito.times(3)).notificarAtraso(Mockito.any(Usuario.class));
		Mockito.verify(emailService).notificarAtraso(usuario);
		Mockito.verify(emailService, Mockito.never()).notificarAtraso(usuario2);
//		Mockito.verify(emailService, Mockito.times(2)).notificarAtraso(usuario3);
		Mockito.verify(emailService, Mockito.atLeastOnce()).notificarAtraso(usuario3);
		Mockito.verifyNoMoreInteractions(emailService);
//		Mockito.verifyZeroInteractions(spcService);
	}
	
	public static void main(String[] args) {
		new BuilderMaster().gerarCodigoClasse(Locacao.class);
	}
}