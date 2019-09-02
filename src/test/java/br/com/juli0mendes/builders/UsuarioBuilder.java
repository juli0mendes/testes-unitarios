package br.com.juli0mendes.builders;

import br.com.juli0mendes.domains.Usuario;

/**
 * 
 * @author Julio Cesar Mendes
 *
 */
public class UsuarioBuilder {

	private Usuario usuario;

	private UsuarioBuilder() {}
	
	public static UsuarioBuilder umUsuario() {
		UsuarioBuilder usuarioBuilder = new UsuarioBuilder();
		usuarioBuilder.usuario = new Usuario();
		usuarioBuilder.usuario.setNome("Usuário 1");
		
		return usuarioBuilder;
	}
	
	public UsuarioBuilder comNome(String nome) {
		this.usuario.setNome(nome);
		return this;
	}
	
	public Usuario build() {
		return usuario;
	}
}
