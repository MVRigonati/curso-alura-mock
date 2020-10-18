package br.com.caelum.leilao.servico;

import java.util.Calendar;
import java.util.List;

import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;
import br.com.caelum.leilao.infra.email.EnviadorDeEmail;

public class EncerradorDeLeilao {

	private int total = 0;
	private EnviadorDeEmail email;
	private final RepositorioDeLeiloes dao;
	
	public EncerradorDeLeilao(final RepositorioDeLeiloes dao, final EnviadorDeEmail carteiro) {
		this.dao = dao;
		this.email = carteiro;
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
				ex.printStackTrace();
			} catch (RuntimeException ex) {
				// Simulando um erro que aconteceria SOMENTE ao executar atualizacao no dao 
				leilao.setEncerrado(false);
				ex.printStackTrace();
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
