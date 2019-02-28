package br.ce.wcaquino.servicos;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;

import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;

public class LocacaoServiceTest {

	private LocacaoService service;

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
		filmes = Arrays.asList(new Filme("Filme 1", 2, 5.0), new Filme("Filme 2", 3, 4.5),
				new Filme("Filme 3", 7, 10.2));
		filmesSemEstoque = Arrays.asList(new Filme("Filme 1", 0, 5.0));
	}

	@Test
	public void deveAlugarFilme() throws Exception {
		// cenario

		Usuario usuario = new Usuario("Usuario 1");

		// acao
		Locacao locacao = service.alugarFilme(usuario, filmes);

		// verificação com rule
		error.checkThat(locacao.getValor(), is(equalTo(17.15)));
		error.checkThat(locacao.getValor(), is(not(6.0)));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterDataComDiferencaDias(1)),
				is(true));
	}

	// forma elegante
	@Test(expected = FilmeSemEstoqueException.class)
	public void naoDeveAlugarFilmeSemEstoque() throws Exception {
		// cenario
		LocacaoService service = new LocacaoService();
		Usuario usuario = new Usuario("Usuario 1");

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
		Usuario usuario = new Usuario("Usuario 1");

		exception.expect(LocadoraException.class);
		exception.expectMessage("Filme vazio");

		// ação
		service.alugarFilme(usuario, null);

//		System.out.println("Forma nova");
	}

	@Test
	public void devePagar75PorCentoNoFilme3() throws FilmeSemEstoqueException, LocadoraException {

		// cenario
		Usuario usuario = new Usuario();

		List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2, 4.0), new Filme("Filme 2", 2, 4.0),
				new Filme("Filme 3", 2, 4.0));

		// acao
		Locacao resultado = service.alugarFilme(usuario, filmes);

		assertThat(resultado.getValor(), is(11.0));
	}

	@Test
	public void devePagar50PorCentoNoFilme4() throws FilmeSemEstoqueException, LocadoraException {

		// cenario
		Usuario usuario = new Usuario();

		List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2, 4.0), new Filme("Filme 2", 2, 4.0),
				new Filme("Filme 3", 2, 4.0), new Filme("Filme 4", 2, 4.0));

		// acao
		Locacao resultado = service.alugarFilme(usuario, filmes);

		assertThat(resultado.getValor(), is(13.0));
	}
	
	@Test
	public void devePagar25PorCentoNoFilme5() throws FilmeSemEstoqueException, LocadoraException {

		// cenario
		Usuario usuario = new Usuario();

		List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2, 4.0), new Filme("Filme 2", 2, 4.0),
				new Filme("Filme 3", 2, 4.0), new Filme("Filme 4", 2, 4.0), new Filme("Filme 5", 2, 4.0));

		// acao
		Locacao resultado = service.alugarFilme(usuario, filmes);

		assertThat(resultado.getValor(), is(14.0));
	}
	
	@Test
	public void devePagar0PorCentoNoFilme6() throws FilmeSemEstoqueException, LocadoraException {

		// cenario
		Usuario usuario = new Usuario();

		List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2, 4.0), new Filme("Filme 2", 2, 4.0),
				new Filme("Filme 3", 2, 4.0), new Filme("Filme 4", 2, 4.0), new Filme("Filme 5", 2, 4.0), new Filme("Filme 6", 2, 4.0));

		// acao
		Locacao resultado = service.alugarFilme(usuario, filmes);

		assertThat(resultado.getValor(), is(14.0));
	}
}