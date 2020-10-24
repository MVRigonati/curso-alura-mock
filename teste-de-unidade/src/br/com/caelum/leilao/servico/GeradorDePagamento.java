package br.com.caelum.leilao.servico;

import java.util.Calendar;
import java.util.List;

import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;
import br.com.caelum.leilao.infra.dao.RepositorioDePagamentos;
import br.com.caelum.leilao.infra.relogio.Relogio;

public class GeradorDePagamento {
	
	final private RepositorioDeLeiloes leiloesRepo;
	final private RepositorioDePagamentos pagamentosRepo;
	final private Relogio relogio;

	public GeradorDePagamento(final RepositorioDeLeiloes leiloesRepo,
			final RepositorioDePagamentos pagamentosRepo, final Relogio relogio) {
		this.leiloesRepo = leiloesRepo;
		this.pagamentosRepo = pagamentosRepo;
		this.relogio = relogio;
	}
	
	public void gera() {
		
		final List<Leilao> encerrados = this.leiloesRepo.encerrados();
		final Avaliador avaliador = new Avaliador();
		
		for (final Leilao leilao : encerrados) {
			avaliador.avalia(leilao);
			
			final Pagamento novoPagamento = new Pagamento(
					avaliador.getMaiorLance(), proximoDiaUtil());
			this.pagamentosRepo.salva(novoPagamento);
		}

	}

	private Calendar proximoDiaUtil() {
		final Calendar proximoDiaUtil = relogio.hoje();
		final int diaDaSemana = proximoDiaUtil.get(Calendar.DAY_OF_WEEK);
		
		int quantidadeDeDiasParaPular = 0;
		if (diaDaSemana == Calendar.SATURDAY) {
			quantidadeDeDiasParaPular = 2;
			
		} else if (diaDaSemana == Calendar.SUNDAY) {
			quantidadeDeDiasParaPular = 1;
			
		}
		
		proximoDiaUtil.add(Calendar.DAY_OF_MONTH, quantidadeDeDiasParaPular);
		return proximoDiaUtil;
	}

}
