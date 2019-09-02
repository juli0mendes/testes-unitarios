package br.com.juli0mendes.services;

import br.com.juli0mendes.domains.Usuario;

public interface SPCService {

	public boolean possuiNegativacao(Usuario usuario) throws Exception;
}