package br.com.juli0mendes.builders;

import static br.com.juli0mendes.builders.FilmeBuilder.umFilme;
import static br.com.juli0mendes.utils.DataUtils.obterDataComDiferencaDias;

import java.util.Arrays;
import java.util.Date;

import br.com.juli0mendes.domains.Filme;
import br.com.juli0mendes.domains.Locacao;
import br.com.juli0mendes.domains.Usuario;


public class LocacaoBuilder {
	private Locacao locacao;
	
	private LocacaoBuilder() {}

	public static LocacaoBuilder umaLocacao() {
		LocacaoBuilder builder = new LocacaoBuilder();
		inicializarDadosPadroes(builder);
		return builder;
	}

	public static void inicializarDadosPadroes(LocacaoBuilder builder) {
		builder.locacao = new Locacao();
		Locacao elemento = builder.locacao;

		
		elemento.setUsuario(UsuarioBuilder.umUsuario().build());
		elemento.setFilmes(Arrays.asList(umFilme().build()));
		elemento.setDataLocacao(new Date());
		elemento.setDataRetorno(obterDataComDiferencaDias(1));
		elemento.setValor(4.0);
	}

	public LocacaoBuilder comUsuario(Usuario param) {
		locacao.setUsuario(param);
		return this;
	}

	public LocacaoBuilder comListaFilmes(Filme... params) {
		locacao.setFilmes(Arrays.asList(params));
		return this;
	}

	public LocacaoBuilder comDataLocacao(Date param) {
		locacao.setDataLocacao(param);
		return this;
	}

	public LocacaoBuilder comDataRetorno(Date param) {
		locacao.setDataRetorno(param);
		return this;
	}
	
	public LocacaoBuilder atrasada() {
		locacao.setDataLocacao(obterDataComDiferencaDias(-4));
		locacao.setDataRetorno(obterDataComDiferencaDias(-2));
		return this;
	}

	public LocacaoBuilder comValor(Double param) {
		locacao.setValor(param);
		return this;
	}

	public Locacao build() {
		return locacao;
	}
}
