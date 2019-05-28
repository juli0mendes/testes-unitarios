package br.ce.wcaquino.servicos;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;

import br.ce.wcaquino.builders.FilmeBuilder;
import br.ce.wcaquino.builders.UsuarioBuilder;
import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;

/**
 * Classe com teste parametrizável.
 * 
 * @author Julio Cesar Mendes
 *
 */
@RunWith(Parameterized.class)
public class CalculoValorLocacaoTest {

	private LocacaoService service;

	@Parameter
	public List<Filme> filmes;

	@Parameter(value = 1)
	public Double valorLocacao;
	
	@Parameter(value = 2)
	public String cenario;
	
	private static Filme filme1 = FilmeBuilder.umFilme().build();
	private static Filme filme2 = FilmeBuilder.umFilme().build();
	private static Filme filme3 = FilmeBuilder.umFilme().build();
	private static Filme filme4 = FilmeBuilder.umFilme().build();
	private static Filme filme5 = FilmeBuilder.umFilme().build();
	private static Filme filme6 = FilmeBuilder.umFilme().build();
	private static Filme filme7 = FilmeBuilder.umFilme().build();

	@Before
	public void setUp() {
		service = new LocacaoService();
		
		LocacaoDAO locacaoDAO = Mockito.mock(LocacaoDAO.class);
		service.setLocacaoDAO(locacaoDAO);
		
		SPCService spcService = Mockito.mock(SPCService.class);
		service.setSpcService(spcService);
	}
	
	@Parameters(name = "{2}")
	public static Collection<Object[]> getParametros() {
		return Arrays.asList(new Object[][] {
			{Arrays.asList(filme1, filme2), 8.0, "2 filmes: sem desconto"},
			{Arrays.asList(filme1, filme2, filme3), 11.0, "3 filmes: 25%"},
			{Arrays.asList(filme1, filme2, filme3, filme4), 13.0, "4 filmes: 55%"},
			{Arrays.asList(filme1, filme2, filme3, filme4, filme5), 14.0, "5 filmes: 75%"},
			{Arrays.asList(filme1, filme2, filme3, filme4, filme5, filme6), 14.0, "6 filmes: 100%"},
			{Arrays.asList(filme1, filme2, filme3, filme4, filme5, filme6, filme7), 18.0, "7 filmes: sem desconto"}
		});
	}

	@Test
	public void deveCalcularValorLocacaoConsiderandoDescontos() throws FilmeSemEstoqueException, LocadoraException {

		// cenario
		Usuario usuario = UsuarioBuilder.umUsuario().build();

		// acao
		Locacao resultado = service.alugarFilme(usuario, filmes);

		assertThat(resultado.getValor(), is(valorLocacao));
	}
}
