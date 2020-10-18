package br.com.caelum.leilao.servico;

import java.util.Calendar;
import java.util.List;

import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;
import br.com.caelum.leilao.infra.dao.RepositorioDePagamentos;

public class GeradorDePagamento {
	
	private RepositorioDeLeiloes leiloesRepo;
	private RepositorioDePagamentos pagamentosRepo;

	public GeradorDePagamento(final RepositorioDeLeiloes leiloesRepo, final RepositorioDePagamentos pagamentosRepo) {
		this.leiloesRepo = leiloesRepo;
		this.pagamentosRepo = pagamentosRepo;
	}
	
	public void gera() {
		
		final List<Leilao> encerrados = this.leiloesRepo.encerrados();
		final Avaliador avaliador = new Avaliador();
		
		for (final Leilao leilao : encerrados) {
			avaliador.avalia(leilao);
			
			final Pagamento novoPagamento = new Pagamento(
					avaliador.getMaiorLance(), Calendar.getInstance());
			this.pagamentosRepo.salva(novoPagamento);
		}

	}

}
