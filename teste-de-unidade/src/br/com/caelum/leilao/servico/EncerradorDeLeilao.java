package br.com.caelum.leilao.servico;

import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;
import br.com.caelum.leilao.infra.email.EnviadorDeEmail;

public class EncerradorDeLeilao {

	private int total = 0;
	private final Logger log;
	private final EnviadorDeEmail email;
	private final RepositorioDeLeiloes dao;
	
	public EncerradorDeLeilao(final RepositorioDeLeiloes dao, final EnviadorDeEmail carteiro, final Logger log) {
		this.dao = dao;
		this.email = carteiro;
		this.log = log;
	}

	public void encerra() {
		List<Leilao> todosLeiloesCorrentes = dao.correntes();

		for (Leilao leilao : todosLeiloesCorrentes) {
			try {
				if (comecouSemanaPassada(leilao)) {
					leilao.setEncerrado(true);
					dao.atualiza(leilao);
					total++;
					email.envia(leilao);
				}
			} catch (NullPointerException ex) {
				// Simulando um erro que aconteceria SOMENTE ao executar enviador de email
				log.severe(ex.toString());
			} catch (RuntimeException ex) {
				// Simulando um erro que aconteceria SOMENTE ao executar atualizacao no dao 
				leilao.setEncerrado(false);
				log.warning(ex.toString());
			}
		}
	}

	private boolean comecouSemanaPassada(Leilao leilao) {
		return diasEntre(leilao.getData(), Calendar.getInstance()) >= 7;
	}

	private int diasEntre(Calendar inicio, Calendar fim) {
		Calendar data = (Calendar) inicio.clone();
		int diasNoIntervalo = 0;
		while (data.before(fim)) {
			data.add(Calendar.DAY_OF_MONTH, 1);
			diasNoIntervalo++;
		}

		return diasNoIntervalo;
	}

	public int getTotalEncerrados() {
		return total;
	}
}
