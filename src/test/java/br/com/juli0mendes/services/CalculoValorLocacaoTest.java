package br.com.juli0mendes.services;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.com.juli0mendes.builders.FilmeBuilder;
import br.com.juli0mendes.builders.UsuarioBuilder;
import br.com.juli0mendes.domains.Filme;
import br.com.juli0mendes.domains.Locacao;
import br.com.juli0mendes.domains.Usuario;
import br.com.juli0mendes.exceptions.FilmeSemEstoqueException;
import br.com.juli0mendes.exceptions.LocadoraException;
import br.com.juli0mendes.repositories.LocacaoDAO;
import br.com.juli0mendes.services.LocacaoService;
import br.com.juli0mendes.services.SPCService;

/**
 * Classe com teste parametrizável.
 * 
 * @author Julio Cesar Mendes
 *
 */
@RunWith(Parameterized.class)
public class CalculoValorLocacaoTest {

	@InjectMocks
	private LocacaoService service;
	
	@Mock
	private LocacaoDAO locacaoDAO;
	
	@Mock
	private SPCService spcService;

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
		MockitoAnnotations.initMocks(this);
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
