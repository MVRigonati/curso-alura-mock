package br.com.caelum.leilao.dominio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;
import br.com.caelum.leilao.servico.EncerradorDeLeilao;

public class EncerradorDeLeilaoTest {
	
	private Leilao leilaoCarroSemanaPassada;
	private Leilao leilaoGeladeiraSemanaPassada;
	
	private Leilao leilaoCarroOntem;
	private Leilao leilaoGeladeiraOntem;
	
	private RepositorioDeLeiloes daoMock;
	
	@Before
	public void before() {
		final Calendar semanaPassada = Calendar.getInstance();
		semanaPassada.add(Calendar.DAY_OF_MONTH, -7);
		final Calendar ontem = Calendar.getInstance();
		ontem.add(Calendar.DAY_OF_MONTH, -1);
		
		final CriadorDeLeilao criadorDeLeilao = new CriadorDeLeilao();
		leilaoCarroSemanaPassada = criadorDeLeilao
				.para("Carro").naData(semanaPassada).constroi();
		leilaoGeladeiraSemanaPassada = criadorDeLeilao
				.para("Geladeira").naData(semanaPassada).constroi();
		leilaoCarroOntem = criadorDeLeilao
				.para("Carro").naData(ontem).constroi();
		leilaoGeladeiraOntem = criadorDeLeilao
				.para("Geladeira").naData(ontem).constroi();
		
		daoMock = mock(RepositorioDeLeiloes.class);
	}
	
	@Test
	public void deveEncerrarLeiloesCriadosAUmaSemana() {
		List<Leilao> leiloesLista = Arrays.asList(leilaoCarroSemanaPassada, leilaoGeladeiraSemanaPassada);
		when(daoMock.correntes()).thenReturn(leiloesLista);
		
		final EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoMock);
		encerrador.encerra();
		
		assertEquals(2, encerrador.getTotalEncerrados());
		assertTrue(leilaoCarroSemanaPassada.isEncerrado());
		assertTrue(leilaoGeladeiraSemanaPassada.isEncerrado());
	}
	
	@Test
	public void naoDeveEncerrarLeilaoCriadoOntem() {
		List<Leilao> leiloesLista = Arrays.asList(leilaoCarroOntem, leilaoGeladeiraOntem);
		when(daoMock.correntes()).thenReturn(leiloesLista);
		
		final EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoMock);
		encerrador.encerra();
		
		assertEquals(0, encerrador.getTotalEncerrados());
		assertFalse(leilaoCarroOntem.isEncerrado());
		assertFalse(leilaoGeladeiraOntem.isEncerrado());
	}

	@Test
	public void naoDeveRealizarAcoesQuandoNaoExistirLeiloesCorrentes() {
		when(daoMock.correntes()).thenReturn(new ArrayList<Leilao>());
		
		final EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoMock);
		encerrador.encerra();
		
		assertEquals(0, encerrador.getTotalEncerrados());
	}
	
}
