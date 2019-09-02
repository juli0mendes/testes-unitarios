package br.com.juli0mendes.repositories;

import java.util.List;

import br.com.juli0mendes.domains.Locacao;

public interface LocacaoDAO {

	public void salvar(Locacao locacao);

	public List<Locacao> obterLocacoesPendentes();
}
