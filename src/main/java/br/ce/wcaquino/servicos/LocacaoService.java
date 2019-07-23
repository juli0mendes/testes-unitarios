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

// TODO: Auto-generated Javadoc
/**
 * The Class LocacaoService.
 */
public class LocacaoService {
	
	/** The locacao DAO. */
	private LocacaoDAO locacaoDAO;
	
	/** The spc service. */
	private SPCService spcService;
	
	/** The email service. */
	private EmailService emailService;
	
	/**
	 * Alugar filme.
	 *
	 * @param usuario the usuario
	 * @param filmes the filmes
	 * @return the locacao
	 * @throws LocadoraException the locadora exception
	 * @throws FilmeSemEstoqueException the filme sem estoque exception
	 */
	public Locacao alugarFilme(Usuario usuario, List<Filme> filmes) throws LocadoraException, FilmeSemEstoqueException {
		
		Double precoTotalLocacao = 0D;
		
		if (usuario == null)
			throw new LocadoraException("Usu�rio vazio");
		
		if (filmes == null || filmes.isEmpty())
			throw new LocadoraException("Filme vazio");
		
		boolean isNegativado;
		
		try {
			isNegativado = spcService.possuiNegativacao(usuario);
		} catch (Exception e) {
			throw new LocadoraException("Problemas no SPC, tente novamente");
		}
		
		if (isNegativado) {
			throw new LocadoraException("Usu�rio negativado");
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
	
	/**
	 * Notificar atrasos.
	 */
	public void notificarAtrasos() {
		List<Locacao> locacoes = this.locacaoDAO.obterLocacoesPendentes();
		
		for (Locacao locacao : locacoes) {
			if (locacao.getDataRetorno().before(new Date()))
				emailService.notificarAtraso(locacao.getUsuario());
		}
	}
	
	/**
	 * Prorrogar locacao.
	 *
	 * @param locacao the locacao
	 * @param dias the dias
	 */
	public void prorrogarLocacao(Locacao locacao, int dias) {
		
		Locacao novaLocacao = new Locacao();
		novaLocacao.setUsuario(locacao.getUsuario());
		novaLocacao.setFilmes(locacao.getFilmes());
		novaLocacao.setDataLocacao(new Date());
		novaLocacao.setDataRetorno(DataUtils.obterDataComDiferencaDias(dias));
		novaLocacao.setValor(locacao.getValor() * dias);
		
		this.locacaoDAO.salvar(novaLocacao);
	}
}