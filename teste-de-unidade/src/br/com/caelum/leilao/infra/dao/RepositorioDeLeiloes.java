package br.com.caelum.leilao.infra.dao;

import java.util.List;

import br.com.caelum.leilao.dominio.Leilao;

public interface RepositorioDeLeiloes {
	
	public void salva(Leilao leilao);
	
    public List<Leilao> encerrados();
    
    public List<Leilao> correntes();
    
    public void atualiza(Leilao leilao);
    
}
