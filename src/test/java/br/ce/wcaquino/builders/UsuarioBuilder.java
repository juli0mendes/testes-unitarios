package br.ce.wcaquino.builders;

import br.ce.wcaquino.entidades.Usuario;

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
	
	public Usuario build() {
		return usuario;
	}
}
