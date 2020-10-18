package br.com.caelum.leilao.infra.email;

import br.com.caelum.leilao.dominio.Leilao;

public interface EnviadorDeEmail {
	
	public void envia(final Leilao leilao);
	
}
