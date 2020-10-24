package br.com.caelum.leilao.servico;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.OCTOBER;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;
import br.com.caelum.leilao.dominio.Usuario;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;
import br.com.caelum.leilao.infra.dao.RepositorioDePagamentos;
import br.com.caelum.leilao.infra.relogio.Relogio;
import br.com.caelum.leilao.infra.relogio.RelogioDoSistema;

public class GeradorDePagamentoTest {
	
	@Mock
	private RepositorioDeLeiloes leiloesRepo;
	@Mock
	private RepositorioDePagamentos pagamentosRepo;
	@Mock
	private Relogio relogio;
	
	private ArgumentCaptor<Pagamento> captor;
	
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
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
		
		final GeradorDePagamento gerador =
				new GeradorDePagamento(leiloesRepo, pagamentosRepo, new RelogioDoSistema());
		gerador.gera();
		
		verify(pagamentosRepo).salva(captor.capture());
		final Pagamento pagamentoGerado = captor.getValue();

		assertEquals(2500.0, pagamentoGerado.getValor(), 0);
	}
	
	@Test
	public void hojeSendoSabadoDeveGerarPagamentoParaOProximoDiaUtil() {
		final Leilao leilaoComDoisLances = criaLeilaoEncerradoComDoisLances();
		when(leiloesRepo.encerrados()).thenReturn(Arrays.asList(leilaoComDoisLances));
		
		final Calendar sabado = Calendar.getInstance();
		sabado.set(2020, OCTOBER, 24);
		when(relogio.hoje()).thenReturn(sabado);
		
		final GeradorDePagamento gerador =
				new GeradorDePagamento(leiloesRepo, pagamentosRepo, relogio);
		gerador.gera();
		
		verify(pagamentosRepo).salva(captor.capture());
		final Pagamento pagamentoGerado = captor.getValue();

		final int segunda = 26;
		assertEquals(segunda, pagamentoGerado.getData().get(DAY_OF_MONTH));
	}
	
	@Test
	public void hojeSendoDomingoDeveGerarPagamentoParaOProximoDiaUtil() {
		final Leilao leilaoComDoisLances = criaLeilaoEncerradoComDoisLances();
		when(leiloesRepo.encerrados()).thenReturn(Arrays.asList(leilaoComDoisLances));
		
		final Calendar domingo = Calendar.getInstance();
		domingo.set(2020, OCTOBER, 25);
		when(relogio.hoje()).thenReturn(domingo);
		
		final GeradorDePagamento gerador =
				new GeradorDePagamento(leiloesRepo, pagamentosRepo, relogio);
		gerador.gera();
		
		verify(pagamentosRepo).salva(captor.capture());
		final Pagamento pagamentoGerado = captor.getValue();

		final int segunda = 26;
		assertEquals(segunda, pagamentoGerado.getData().get(DAY_OF_MONTH));
	}

}
