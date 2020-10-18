package br.com.caelum.leilao.servico;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;
import br.com.caelum.leilao.dominio.Usuario;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;
import br.com.caelum.leilao.infra.dao.RepositorioDePagamentos;

public class GeradorDePagamentoTest {
	
	private RepositorioDeLeiloes leiloesRepo;
	private RepositorioDePagamentos pagamentosRepo;
	private ArgumentCaptor<Pagamento> captor;
	
	@Before
	public void before() {
		leiloesRepo = mock(RepositorioDeLeiloes.class);
		pagamentosRepo = mock(RepositorioDePagamentos.class);
		
		captor = ArgumentCaptor.forClass(Pagamento.class);
	}
	
	private Leilao criaLeilaoEncerradoComDoisLances() {
		return new CriadorDeLeilao().para("L1")
				.lance(new Usuario("U1"), 2000)
				.lance(new Usuario("U2"), 2500)
				.encerrado().constroi();
	}
	
	@Test
	public void deveGerarPagamentoParaLeilaoEncerradoComOValorCorreto() {
		final Leilao leilaoComDoisLances = criaLeilaoEncerradoComDoisLances();
		when(leiloesRepo.encerrados()).thenReturn(Arrays.asList(leilaoComDoisLances));
		
		final GeradorDePagamento gerador = new GeradorDePagamento(leiloesRepo, pagamentosRepo);
		gerador.gera();
		
		verify(pagamentosRepo).salva(captor.capture());
		final Pagamento pagamentoGerado = captor.getValue();

		assertEquals(2500.0, pagamentoGerado.getValor(), 0);
	}

}
