package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.utils.DataUtils.adicionarDias;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;

public class LocacaoService {
	
	private LocacaoDAO locacaoDAO;
	
	private SPCService spcService;
	
	public Locacao alugarFilme(Usuario usuario, List<Filme> filmes) throws LocadoraException, FilmeSemEstoqueException {
		
		Double precoTotalLocacao = 0D;
		
		if (usuario == null)
			throw new LocadoraException("Usuário vazio");
		
		if (filmes == null || filmes.isEmpty())
			throw new LocadoraException("Filme vazio");
		
		if (spcService.possuiNegativacao(usuario)) {
			throw new LocadoraException("Usuário negativado");
		}
		
		for (int i = 0; i < filmes.size(); i++) {

			Filme filme = filmes.get(i);
			
			if (filme.getEstoque() == 0)
				throw new FilmeSemEstoqueException();
			
			Double valorFilme = filme.getPrecoLocacao();
			
			switch (i) {
			case 2:
				valorFilme = valorFilme * 0.75;
				break;
			case 3:
				valorFilme = valorFilme * 0.50;
				break;
			case 4:
				valorFilme = valorFilme * 0.25;
				break;
			case 5:
				valorFilme = 0D;
				break;
			}
			
			precoTotalLocacao += valorFilme;
		}
			
		Locacao locacao = new Locacao();
		locacao.setFilmes(filmes);
		locacao.setUsuario(usuario);
		locacao.setDataLocacao(new Date());
		locacao.setValor(precoTotalLocacao);

		//Entrega no dia seguinte
		Date dataEntrega = new Date();
		dataEntrega = adicionarDias(dataEntrega, 1);
		
		// se data da entrega for domingo
		if (DataUtils.verificarDiaSemana(dataEntrega, Calendar.SUNDAY))
			dataEntrega = adicionarDias(dataEntrega, 1);
		
		locacao.setDataRetorno(dataEntrega);
		
		//Salvando a locacao...	
		this.locacaoDAO.salvar(locacao);
		
		return locacao;
	}
	
	public void setLocacaoDAO(LocacaoDAO locacaoDAO) {
		this.locacaoDAO = locacaoDAO;
	}
	
	public void setSpcService(SPCService spcService) {
		this.spcService = spcService;
	}
}